package cgl.iotcloud.core.master;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.api.thrift.*;
import cgl.iotcloud.core.master.events.MSensorSiteEvent;
import cgl.iotcloud.core.master.thrift.TMasterService;
import cgl.iotcloud.core.master.thrift.TRegisterSiteRequest;
import cgl.iotcloud.core.sensor.ChannelDetails;
import cgl.iotcloud.core.sensor.SensorDetails;
import cgl.iotcloud.core.sensorsite.SensorState;
import cgl.iotcloud.core.transport.Direction;
import com.google.common.eventbus.EventBus;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class MasterServiceHandler implements TMasterService.Iface {
    private static Logger LOG = LoggerFactory.getLogger(MasterServiceHandler.class);

    private MasterContext masterContext;

    private BlockingQueue<SiteEvent> siteEventsQueue;

    private EventBus sensorEventBus;

    public MasterServiceHandler(MasterContext masterContext,
                                BlockingQueue<SiteEvent> siteEventsQueue,
                                EventBus sensorEventBus) {
        this.masterContext = masterContext;
        this.siteEventsQueue = siteEventsQueue;
        this.sensorEventBus = sensorEventBus;
    }

    @Override
    public TResponse registerSite(TRegisterSiteRequest request) throws TException {
        String id = request.getSiteId();
        String host = request.getHost();
        int port = request.getPort();

        SiteDescriptor descriptor = new SiteDescriptor(id, port, host);
        descriptor.setMetadata(request.getMetadata());

        masterContext.addSensorSite(descriptor);

        // notify the monitor about the new site
        SiteEvent siteEvent = new SiteEvent(id, SiteEvent.State.ADDED);
        try {
            siteEventsQueue.put(siteEvent);
        } catch (InterruptedException e) {
            masterContext.removeSite(id);
            LOG.error("Failed to add the new site..");
        }

        TResponse registerSiteResponse = new TResponse();
        registerSiteResponse.setState(TResponseState.SUCCESS);
        return registerSiteResponse;
    }

    @Override
    public TResponse registerSensor(String siteId, TSensor sensor) throws TException {
        TSensorId id = sensor.getId();
        SensorId sensorID = new SensorId(id.getName(), id.getGroup());

        SensorDetails sensorDetails = new SensorDetails(sensorID);
        if (sensor.getChannels() != null) {
            for (TChannel tChannel : sensor.getChannels()) {
                ChannelDetails details = null;
                if (tChannel.getDirection() == TDirection.IN) {
                    details = new ChannelDetails(Direction.IN);
                } else if (tChannel.getDirection() == TDirection.OUT) {
                    details = new ChannelDetails(Direction.OUT);
                }
                sensorDetails.addChannel(tChannel.getTransport(), details);
            }
        } else {
            LOG.warn("Sensor registered with no channels {}", id);
        }
        sensorDetails.setMetadata(sensor.getMetadata());

        MSensorSiteEvent updateEvent = new MSensorSiteEvent(sensorID, SensorState.DEPLOY, siteId);
        updateEvent.setSensorDetails(sensorDetails);

        sensorEventBus.post(updateEvent);
        return new TResponse(TResponseState.SUCCESS, "successfully added");
    }

    @Override
    public TResponse unRegisterSensor(String siteId, TSensorId id) throws TException {
        SensorId sensorID = new SensorId(id.getName(), id.getGroup());
        MSensorSiteEvent updateEvent = new MSensorSiteEvent(sensorID, SensorState.UN_DEPLOY, siteId);
        sensorEventBus.post(updateEvent);
        return new TResponse(TResponseState.SUCCESS, "successfully un deployed");
    }

    @Override
    public TResponse updateSensor(String siteId, TSensor sensor) throws TException {
        TSensorId id = sensor.getId();
        SensorId sensorID = new SensorId(id.getName(), id.getGroup());
        SensorDetails sensorDetails = new SensorDetails(sensorID);

        if (sensor.getChannels() != null) {
            for (TChannel tChannel : sensor.getChannels()) {
                ChannelDetails details = null;
                if (tChannel.getDirection() == TDirection.IN) {
                    details = new ChannelDetails(Direction.IN);
                } else if (tChannel.getDirection() == TDirection.OUT) {
                    details = new ChannelDetails(Direction.OUT);
                }
                sensorDetails.addChannel(tChannel.getTransport(), details);
            }
        } else {
            LOG.warn("Sensor registered with no channels {}", id);
        }

        sensorDetails.setMetadata(sensor.getMetadata());
        MSensorSiteEvent updateEvent;
        if (sensor.getState() == TSensorState.UPDATE) {
            updateEvent = new MSensorSiteEvent(sensorID, SensorState.UPDATE, siteId);
        } else if (sensor.getState() == TSensorState.ACTIVE) {
            updateEvent = new MSensorSiteEvent(sensorID, SensorState.ACTIVATE, siteId);
        } else if (sensor.getState() == TSensorState.DE_ACTIVATE) {
            updateEvent = new MSensorSiteEvent(sensorID, SensorState.DEACTIVATE, siteId);
        } else {
            updateEvent = new MSensorSiteEvent(sensorID, SensorState.UPDATE, siteId);
        }
        updateEvent.setSensorDetails(sensorDetails);

        sensorEventBus.post(updateEvent);
        return new TResponse(TResponseState.SUCCESS, "successfully un deployed");
    }
}
