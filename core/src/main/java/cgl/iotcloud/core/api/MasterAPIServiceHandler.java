package cgl.iotcloud.core.api;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.api.thrift.*;
import cgl.iotcloud.core.master.MasterContext;
import cgl.iotcloud.core.master.SiteDescriptor;
import cgl.iotcloud.core.master.events.MasterSensorDeployEvent;
import cgl.iotcloud.core.master.events.MasterSensorStartEvent;
import cgl.iotcloud.core.master.events.MasterSensorStopEvent;
import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;
import com.google.common.eventbus.EventBus;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MasterAPIServiceHandler implements TMasterAPIService.Iface {
    private static Logger LOG = LoggerFactory.getLogger(MasterAPIServiceHandler.class);

    private MasterContext masterContext;

    private EventBus sensorEventBus;

    public MasterAPIServiceHandler(MasterContext masterContext, EventBus sensorEventBus) {
        this.masterContext = masterContext;
        this.sensorEventBus = sensorEventBus;
    }

    @Override
    public List<String> getSites() throws TException {
        return new ArrayList<String>(masterContext.getSensorSites().keySet());
    }

    @Override
    public TSiteDetailsResponse getSite(String siteId) throws TException {
        SiteDescriptor descriptor = masterContext.getSensorSite(siteId);

        if (descriptor != null) {
            TSiteDetails siteDetails = new TSiteDetails(siteId, descriptor.getPort(), descriptor.getHost());
            // siteDetails.setMetadata(details.getMetadata());
            TSiteDetailsResponse response = new TSiteDetailsResponse();
            response.setDetails(siteDetails);
            response.setState(new TResponse(TResponseState.SUCCESS, "success"));

            return response;
        }

        TSiteDetailsResponse response = new TSiteDetailsResponse();
        response.setDetails(null);
        response.setState(new TResponse(TResponseState.FAILURE, "no site with the given site id present"));

        return response;
    }

    @Override
    public TResponse deploySensor(List<String> sites, TSensorDetails sensor) throws TException {
        SensorDeployDescriptor deployDescriptor = new SensorDeployDescriptor(sensor.getFilename(), sensor.getClassName());
        deployDescriptor.addDeploySites(sites);

        for (Map.Entry<String, String> e : sensor.getProperties().entrySet()) {
            deployDescriptor.addProperty(e.getKey(), e.getValue());
        }

        MasterSensorDeployEvent deployEvent = new MasterSensorDeployEvent(sites, deployDescriptor);
        sensorEventBus.post(deployEvent);
        return new TResponse(TResponseState.SUCCESS, "success");
    }

    @Override
    public TResponse startSensor(List<String> sites, TSensorId id) throws TException {
        SensorId sensorId = new SensorId(id.getName(), id.getGroup());
        MasterSensorStartEvent sensorStopEvent = new MasterSensorStartEvent(sensorId, sites);
        sensorEventBus.post(sensorStopEvent);
        return new TResponse(TResponseState.SUCCESS, "success");
    }

    @Override
    public TResponse startAllSensor(TSensorId id) throws TException {
        SensorId sensorId = new SensorId(id.getName(), id.getGroup());
        MasterSensorStartEvent sensorStopEvent = new MasterSensorStartEvent(sensorId);
        sensorEventBus.post(sensorStopEvent);
        return new TResponse(TResponseState.SUCCESS, "success");
    }

    @Override
    public TResponse stopSiteSensors(List<String> sites, TSensorId id) throws TException {
        SensorId sensorId = new SensorId(id.getName(), id.getGroup());
        MasterSensorStopEvent sensorStopEvent = new MasterSensorStopEvent(sensorId, sites);
        sensorEventBus.post(sensorStopEvent);
        return new TResponse(TResponseState.SUCCESS, "success");
    }

    @Override
    public TResponse stopAllSensors(TSensorId id) throws TException {
        SensorId sensorId = new SensorId(id.getName(), id.getGroup());
        MasterSensorStopEvent sensorStopEvent = new MasterSensorStopEvent(sensorId);
        sensorEventBus.post(sensorStopEvent);
        return new TResponse(TResponseState.SUCCESS, "success");
    }

    @Override
    public List<TSensorDetails> getSensors(String siteId) throws TException {
        return null;
    }
}
