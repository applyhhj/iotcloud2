package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.api.thrift.*;
import cgl.iotcloud.core.master.thrift.THeartBeatRequest;
import cgl.iotcloud.core.master.thrift.THeartBeatResponse;
import cgl.iotcloud.core.sensorsite.thrift.TSensorSiteService;
import org.apache.log4j.helpers.LogLog;
import org.apache.thrift.TException;

import java.util.concurrent.BlockingQueue;

public class SensorSiteService implements TSensorSiteService.Iface {
    private SiteContext siteContext;

    private BlockingQueue<SensorEvent> sensorEvents;

    public SensorSiteService(SiteContext siteContext, BlockingQueue<SensorEvent> sensorEvents) {
        this.siteContext = siteContext;
        this.sensorEvents = sensorEvents;
    }

    @Override
    public THeartBeatResponse hearbeat(THeartBeatRequest heartBeat) throws TException {
        THeartBeatResponse response = new THeartBeatResponse();
        response.setTotalSensors(siteContext.getRegisteredSensors().size());
        return response;
    }

    @Override
    public TResponse deploySensor(TSensorDetails sensor) throws TException {
        LogLog.debug("Request received for deploying a sensor {}" + sensor);

        String className = sensor.getClassName();
        String jarName = sensor.getFilename();

        SensorDeployDescriptor deployDescriptor = new SensorDeployDescriptor(jarName, className);
        SensorEvent event = new SensorEvent(deployDescriptor, SensorEventState.DEPLOY);

        try {
            sensorEvents.put(event);
        } catch (InterruptedException e) {
            return new TResponse(TResponseState.FAILURE, "sensor cannot be scheduled for deployment");
        }
        return new TResponse(TResponseState.SUCCESS, "sensor is scheduled to be deployed");
    }

    @Override
    public TResponse startSensor(TSensorId id) throws TException {
        LogLog.debug("Request received for starting a sensor with ID {}" + id);

        SensorEvent event = new SensorEvent(new SensorId(id.getName(), id.getGroup()),
                SensorEventState.ACTIVATE);
        try {
            sensorEvents.put(event);
        } catch (InterruptedException e) {
            return new TResponse(TResponseState.FAILURE, "sensor cannot be scheduled for activation");
        }
        return new TResponse(TResponseState.SUCCESS, "sensor is scheduled to for activation");
    }

    @Override
    public TResponse stopSensor(TSensorId id) throws TException {
        LogLog.debug("Request received for stopping a sensor with ID {}" + id);

        SensorEvent event = new SensorEvent(new SensorId(id.getName(), id.getGroup()),
                SensorEventState.DEACTIVATE);
        try {
            sensorEvents.put(event);
        } catch (InterruptedException e) {
            return new TResponse(TResponseState.FAILURE, "sensor cannot be scheduled for de-activation");
        }
        return new TResponse(TResponseState.SUCCESS, "sensor is scheduled to for de-activation");
    }
}
