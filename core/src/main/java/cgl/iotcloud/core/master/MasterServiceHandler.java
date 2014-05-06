package cgl.iotcloud.core.master;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.api.thrift.*;
import cgl.iotcloud.core.master.thrift.TMasterService;
import cgl.iotcloud.core.master.thrift.TRegisterSiteRequest;
import cgl.iotcloud.core.sensorsite.SensorEventState;
import cgl.iotcloud.core.transport.Direction;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class MasterServiceHandler implements TMasterService.Iface {
    private static Logger LOG = LoggerFactory.getLogger(MasterServiceHandler.class);

    private MasterContext masterContext;

    private BlockingQueue<SiteEvent> siteEventsQueue;

    private BlockingQueue<MasterSensorEvent> sensorEvents;

    public MasterServiceHandler(MasterContext masterContext,
                                BlockingQueue<SiteEvent> siteEventsQueue,
                                BlockingQueue<MasterSensorEvent> sensorEvents) {
        this.masterContext = masterContext;
        this.siteEventsQueue = siteEventsQueue;
        this.sensorEvents = sensorEvents;
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
        // TODO check
        sensorDetails.setMetadata(sensor.getMetadata());

        if (masterContext.addSensor(siteId, sensorDetails)) {
            MasterSensorEvent event = new MasterSensorEvent(sensorID, SensorEventState.ADD);
            sensorEvents.add(event);
            return new TResponse(TResponseState.SUCCESS, "successfully added");
        } else {
            return new TResponse(TResponseState.FAILURE, "Failed to add the sensor");
        }
    }

    @Override
    public TResponse unRegisterSensor(String siteId, TSensorId id) throws TException {
        SensorId sensorID = new SensorId(id.getName(), id.getGroup());
        if (masterContext.removeSensor(siteId, sensorID)) {
            sensorEvents.add(new MasterSensorEvent(sensorID, SensorEventState.REMOVE));
            return new TResponse(TResponseState.SUCCESS, "successfully added");
        } else {
            return new TResponse(TResponseState.FAILURE, "Failed to remove the sensor");
        }
    }

    @Override
    public TResponse updateSensor(String siteId, TSensor sensor) throws TException {
        return null;
    }
}
