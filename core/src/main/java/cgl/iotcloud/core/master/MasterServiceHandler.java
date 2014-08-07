package cgl.iotcloud.core.master;

import cgl.iotcloud.core.api.thrift.*;
import cgl.iotcloud.core.master.events.MSensorSiteEvent;
import cgl.iotcloud.core.master.events.MSiteEvent;
import cgl.iotcloud.core.master.thrift.TMasterService;
import cgl.iotcloud.core.sensorsite.SensorState;
import com.google.common.eventbus.EventBus;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterServiceHandler implements TMasterService.Iface {
    private static Logger LOG = LoggerFactory.getLogger(MasterServiceHandler.class);

    private EventBus siteEventBus;

    public MasterServiceHandler(EventBus siteEventBus) {
        this.siteEventBus = siteEventBus;
    }

    @Override
    public TResponse registerSite(TSite request) throws TException {
        // notify the monitor about the new site
        MSiteEvent siteEvent = new MSiteEvent(request.getSiteId(), SiteState.ADDED, request);
        siteEventBus.post(siteEvent);

        TResponse registerSiteResponse = new TResponse();
        registerSiteResponse.setState(TResponseState.SUCCESS);
        return registerSiteResponse;
    }

    @Override
    public TResponse unRegisterSite(TSite site) throws TException {
        String id = site.getSiteId();
        // notify the monitor about the new site
        MSiteEvent siteEvent = new MSiteEvent(id, SiteState.REMOVED);
        siteEventBus.post(siteEvent);

        TResponse registerSiteResponse = new TResponse();
        registerSiteResponse.setState(TResponseState.SUCCESS);
        return registerSiteResponse;
    }

    @Override
    public TResponse registerSensor(String siteId, TSensor sensor) throws TException {
        String id = sensor.getName();

        LOG.info("Request received for registering a sensor from site {} with sensor id {}", siteId, id);

        MSensorSiteEvent updateEvent = new MSensorSiteEvent(id, SensorState.DEPLOY, siteId);
        updateEvent.setSensor(sensor);

        siteEventBus.post(updateEvent);
        return new TResponse(TResponseState.SUCCESS, "successfully added");
    }

    @Override
    public TResponse unRegisterSensor(String siteId, String id) throws TException {
        LOG.info("Request received for un-registering a sensor from site {} with sensor id {}", siteId, id);

        MSensorSiteEvent updateEvent = new MSensorSiteEvent(id, SensorState.UN_DEPLOY, siteId);
        siteEventBus.post(updateEvent);
        return new TResponse(TResponseState.SUCCESS, "successfully un deployed");
    }

    @Override
    public TResponse updateSensor(String siteId, TSensor sensor) throws TException {
        String id = sensor.getName();

        LOG.info("Request received for updating a sensor from site {} with sensor id {}", siteId, id);
        MSensorSiteEvent updateEvent;
        if (sensor.getState() == TSensorState.DEPLOY) {
            updateEvent = new MSensorSiteEvent(id, SensorState.DEPLOY, siteId);
        } else if (sensor.getState() == TSensorState.ACTIVE) {
            updateEvent = new MSensorSiteEvent(id, SensorState.ACTIVATE, siteId);
        } else if (sensor.getState() == TSensorState.DE_ACTIVATE) {
            updateEvent = new MSensorSiteEvent(id, SensorState.DEACTIVATE, siteId);
        } else if (sensor.getState() == TSensorState.UN_DEPLOY) {
            updateEvent = new MSensorSiteEvent(id, SensorState.UN_DEPLOY, siteId);
        } else {
            updateEvent = new MSensorSiteEvent(id, SensorState.UPDATE, siteId);
        }
        updateEvent.setSensor(sensor);

        siteEventBus.post(updateEvent);
        return new TResponse(TResponseState.SUCCESS, "successfully un deployed");
    }
}
