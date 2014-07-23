package cgl.iotcloud.core.master;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.api.thrift.*;
import cgl.iotcloud.core.desc.ChannelDescriptor;
import cgl.iotcloud.core.desc.SensorDescriptor;
import cgl.iotcloud.core.desc.SiteDescriptor;
import cgl.iotcloud.core.master.events.MSensorSiteEvent;
import cgl.iotcloud.core.master.events.MSiteEvent;
import cgl.iotcloud.core.master.thrift.TMasterService;
import cgl.iotcloud.core.master.thrift.TRegisterSiteRequest;
import cgl.iotcloud.core.master.thrift.TSite;
import cgl.iotcloud.core.sensorsite.SensorState;
import cgl.iotcloud.core.transport.Direction;
import com.google.common.eventbus.EventBus;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterServiceHandler implements TMasterService.Iface {
    private static Logger LOG = LoggerFactory.getLogger(MasterServiceHandler.class);

    private EventBus sensorEventBus;

    private EventBus siteEventBus;

    public MasterServiceHandler(EventBus siteEventBus,
                                EventBus sensorEventBus) {
        this.siteEventBus = siteEventBus;
        this.sensorEventBus = sensorEventBus;
    }

    @Override
    public TResponse registerSite(TRegisterSiteRequest request) throws TException {
        String id = request.getSiteId();
        String host = request.getHost();
        int port = request.getPort();

        SiteDescriptor descriptor = new SiteDescriptor(id, port, host);
        descriptor.setMetadata(request.getMetadata());

        // notify the monitor about the new site
        MSiteEvent siteEvent = new MSiteEvent(id, SiteState.ADDED, descriptor);
        siteEventBus.post(siteEvent);

        TResponse registerSiteResponse = new TResponse();
        registerSiteResponse.setState(TResponseState.SUCCESS);
        return registerSiteResponse;
    }

    @Override
    public TResponse unRegisterSite(TSite site) throws TException {
        String id = site.getSiteid();

        // notify the monitor about the new site
        MSiteEvent siteEvent = new MSiteEvent(id, SiteState.REMOVED);
        siteEventBus.post(siteEvent);

        TResponse registerSiteResponse = new TResponse();
        registerSiteResponse.setState(TResponseState.SUCCESS);
        return registerSiteResponse;
    }

    @Override
    public TResponse registerSensor(String siteId, TSensor sensor) throws TException {

        TSensorId id = sensor.getId();
        SensorId sensorID = new SensorId(id.getName(), id.getGroup());

        LOG.info("Request received for registering a sensor from site {} with sensor id {}", siteId, sensorID);

        SensorDescriptor sensorDescriptor = new SensorDescriptor(sensorID);
        if (sensor.getChannels() != null) {
            for (TChannel tChannel : sensor.getChannels()) {
                ChannelDescriptor details = null;
                if (tChannel.getDirection() == TDirection.IN) {
                    details = new ChannelDescriptor(Direction.IN);
                } else if (tChannel.getDirection() == TDirection.OUT) {
                    details = new ChannelDescriptor(Direction.OUT);
                }
                sensorDescriptor.addChannel(tChannel.getTransport(), details);
            }
        } else {
            LOG.warn("Sensor registered with no channels {}", id);
        }
        sensorDescriptor.setMetadata(sensor.getMetadata());

        MSensorSiteEvent updateEvent = new MSensorSiteEvent(sensorID, SensorState.DEPLOY, siteId);
        updateEvent.setSensorDescriptor(sensorDescriptor);

        sensorEventBus.post(updateEvent);
        return new TResponse(TResponseState.SUCCESS, "successfully added");
    }

    @Override
    public TResponse unRegisterSensor(String siteId, TSensorId id) throws TException {
        SensorId sensorID = new SensorId(id.getName(), id.getGroup());

        LOG.info("Request received for un-registering a sensor from site {} with sensor id {}", siteId, sensorID);

        MSensorSiteEvent updateEvent = new MSensorSiteEvent(sensorID, SensorState.UN_DEPLOY, siteId);
        sensorEventBus.post(updateEvent);
        return new TResponse(TResponseState.SUCCESS, "successfully un deployed");
    }

    @Override
    public TResponse updateSensor(String siteId, TSensor sensor) throws TException {
        TSensorId id = sensor.getId();
        SensorId sensorID = new SensorId(id.getName(), id.getGroup());

        LOG.info("Request received for updating a sensor from site {} with sensor id {}", siteId, sensorID);

        SensorDescriptor sensorDescriptor = new SensorDescriptor(sensorID);

        if (sensor.getChannels() != null) {
            for (TChannel tChannel : sensor.getChannels()) {
                ChannelDescriptor details = null;
                if (tChannel.getDirection() == TDirection.IN) {
                    details = new ChannelDescriptor(Direction.IN);
                } else if (tChannel.getDirection() == TDirection.OUT) {
                    details = new ChannelDescriptor(Direction.OUT);
                }
                sensorDescriptor.addChannel(tChannel.getTransport(), details);
            }
        } else {
            LOG.warn("Sensor registered with no channels {}", id);
        }

        sensorDescriptor.setMetadata(sensor.getMetadata());
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
        updateEvent.setSensorDescriptor(sensorDescriptor);

        sensorEventBus.post(updateEvent);
        return new TResponse(TResponseState.SUCCESS, "successfully un deployed");
    }
}
