package cgl.iotcloud.core.api;

import cgl.iotcloud.core.api.thrift.*;
import cgl.iotcloud.core.master.MasterContext;
import cgl.iotcloud.core.desc.SiteDescriptor;
import cgl.iotcloud.core.master.events.MSensorClientEvent;
import cgl.iotcloud.core.sensorsite.SensorState;
import com.google.common.eventbus.EventBus;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
            TSite siteDetails = new TSite(siteId, descriptor.getPort(), descriptor.getHost());
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
    public TResponse deploySensor(List<String> sites, TSensorDeployDescriptor sensor) throws TException {
        LOG.info("Request received for deploying a sensor with jar {} and class {}", sensor.getFilename(), sensor.getClassName());
        MSensorClientEvent deployEvent = new MSensorClientEvent(SensorState.DEPLOY, sensor, sites);
        sensorEventBus.post(deployEvent);
        return new TResponse(TResponseState.SUCCESS, "success");
    }

    @Override
    public TResponse unDeploySensor(List<String> sites, String id) throws TException {
        LOG.info("Request received for un-deploying a sensor {}", id);
        MSensorClientEvent deployEvent = new MSensorClientEvent(id, SensorState.UN_DEPLOY, sites);
        sensorEventBus.post(deployEvent);
        return new TResponse(TResponseState.SUCCESS, "success");
    }

    @Override
    public TResponse unDeployAllSensor(String id) throws TException {
        LOG.info("Request received for un-deploying a sensor {}", id);
        MSensorClientEvent deployEvent = new MSensorClientEvent(id, SensorState.UN_DEPLOY);
        sensorEventBus.post(deployEvent);
        return new TResponse(TResponseState.SUCCESS, "success");
    }

    @Override
    public TResponse startSensor(List<String> sites, String id) throws TException {
        LOG.info("Request received for starting a sensor {}", id);
        MSensorClientEvent sensorStopEvent = new MSensorClientEvent(id, SensorState.ACTIVATE, sites);
        sensorEventBus.post(sensorStopEvent);
        return new TResponse(TResponseState.SUCCESS, "success");
    }

    @Override
    public TResponse startAllSensor(String id) throws TException {
        LOG.info("Request received for starting a sensor {}", id);
        MSensorClientEvent sensorStopEvent = new MSensorClientEvent(id, SensorState.ACTIVATE);
        sensorEventBus.post(sensorStopEvent);
        return new TResponse(TResponseState.SUCCESS, "success");
    }

    @Override
    public TResponse stopSiteSensors(List<String> sites, String id) throws TException {
        LOG.info("Request received for stopping a sensor {}", id);
        MSensorClientEvent sensorStopEvent = new MSensorClientEvent(id, SensorState.DEACTIVATE, sites);
        sensorEventBus.post(sensorStopEvent);
        return new TResponse(TResponseState.SUCCESS, "success");
    }

    @Override
    public TResponse stopAllSensors(String id) throws TException {
        LOG.info("Request received for starting a sensor {}", id);
        MSensorClientEvent sensorStopEvent = new MSensorClientEvent(id, SensorState.DEACTIVATE);
        sensorEventBus.post(sensorStopEvent);
        return new TResponse(TResponseState.SUCCESS, "success");
    }

    @Override
    public List<TSensor> getSensors(String siteId) throws TException {

        return null;
    }
}
