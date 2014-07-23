package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.api.thrift.*;
import cgl.iotcloud.core.master.thrift.THeartBeatRequest;
import cgl.iotcloud.core.master.thrift.THeartBeatResponse;
import cgl.iotcloud.core.sensorsite.events.SensorEvent;
import cgl.iotcloud.core.sensorsite.thrift.TSensorSiteService;
import com.google.common.eventbus.EventBus;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SensorSiteService implements TSensorSiteService.Iface {
    private Logger LOG = LoggerFactory.getLogger(SensorSiteService.class);

    private SiteContext siteContext;

    private EventBus sensorEvents;

    public SensorSiteService(SiteContext siteContext, EventBus sensorEventBus) {
        this.siteContext = siteContext;
        this.sensorEvents = sensorEventBus;
    }

    @Override
    public THeartBeatResponse hearbeat(THeartBeatRequest heartBeat) throws TException {
        THeartBeatResponse response = new THeartBeatResponse();
        response.setTotalSensors(siteContext.getRegisteredSensors().size());
        return response;
    }

    @Override
    public TResponse deploySensor(TSensorDeployDescriptor sensor) throws TException {
        LOG.info("Request received for deploying a sensor {}" + sensor);

        String className = sensor.getClassName();
        String jarName = sensor.getFilename();

        SensorDeployDescriptor deployDescriptor = new SensorDeployDescriptor(jarName, className);
        if (sensor.getProperties() != null) {
            for (Map.Entry<String, String> e : sensor.getProperties().entrySet()) {
                deployDescriptor.addProperty(e.getKey(), e.getValue());
            }
        }

        SensorEvent event = new SensorEvent(deployDescriptor, SensorState.DEPLOY);
        sensorEvents.post(event);
        return new TResponse(TResponseState.SUCCESS, "sensor is scheduled to be deployed");
    }

    @Override
    public TResponse unDeploySensor(TSensorId id) throws TException {
        LOG.info("Request received for Un-Deploying a sensor with ID {}" + id);
        SensorEvent event = new SensorEvent(new SensorId(id.getName(), id.getGroup()), SensorState.UN_DEPLOY);

        sensorEvents.post(event);
        return new TResponse(TResponseState.SUCCESS, "sensor is scheduled to be deployed");
    }

    @Override
    public TResponse startSensor(TSensorId id) throws TException {
        LOG.info("Request received for starting a sensor with ID {}" + id);

        SensorEvent event = new SensorEvent(new SensorId(id.getName(), id.getGroup()),
                SensorState.ACTIVATE);
        sensorEvents.post(event);
        return new TResponse(TResponseState.SUCCESS, "sensor is scheduled to for activation");
    }

    @Override
    public TResponse stopSensor(TSensorId id) throws TException {
        LOG.info("Request received for stopping a sensor with ID {}" + id);

        SensorEvent event = new SensorEvent(new SensorId(id.getName(), id.getGroup()),
                SensorState.DEACTIVATE);
        sensorEvents.post(event);
        return new TResponse(TResponseState.SUCCESS, "sensor is scheduled to for de-activation");
    }
}
