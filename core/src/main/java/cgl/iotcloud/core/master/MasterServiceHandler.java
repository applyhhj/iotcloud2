package cgl.iotcloud.core.master;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.api.thrift.*;
import cgl.iotcloud.core.master.events.MSensorUpdateEvent;
import cgl.iotcloud.core.master.thrift.TMasterService;
import cgl.iotcloud.core.master.thrift.TRegisterSiteRequest;
import cgl.iotcloud.core.sensorsite.SensorEventState;
import cgl.iotcloud.core.transport.Direction;
import com.google.common.eventbus.EventBus;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
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

        MSensorUpdateEvent updateEvent = new MSensorUpdateEvent(sensorID, Arrays.asList(siteId), SensorEventState.ACTIVATE);
        updateEvent.setSensorDetails(sensorDetails);

        sensorEventBus.post(updateEvent);
        return new TResponse(TResponseState.SUCCESS, "successfully added");
    }

    @Override
    public TResponse unRegisterSensor(String siteId, TSensorId id) throws TException {
        SensorId sensorID = new SensorId(id.getName(), id.getGroup());
        MSensorUpdateEvent updateEvent = new MSensorUpdateEvent(sensorID, Arrays.asList(siteId), SensorEventState.UN_DEPLOY);
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
        MSensorUpdateEvent updateEvent;
        if (sensor.getState() == TSensorState.UPDATE) {
            updateEvent = new MSensorUpdateEvent(sensorID, Arrays.asList(siteId), SensorEventState.UPDATE);
        } else if (sensor.getState() == TSensorState.ACTIVE) {
            updateEvent = new MSensorUpdateEvent(sensorID, Arrays.asList(siteId), SensorEventState.ACTIVATE);
        } else if (sensor.getState() == TSensorState.DE_ACTIVATE) {
            updateEvent = new MSensorUpdateEvent(sensorID, Arrays.asList(siteId), SensorEventState.DEACTIVATE);
        } else {
            updateEvent = new MSensorUpdateEvent(sensorID, Arrays.asList(siteId), SensorEventState.UPDATE);
        }
        updateEvent.setSensorDetails(sensorDetails);

        sensorEventBus.post(updateEvent);
        return new TResponse(TResponseState.SUCCESS, "successfully un deployed");
    }
}
