package cgl.iotcloud.core.api;

import cgl.iotcloud.core.api.thrift.*;
import cgl.iotcloud.core.master.MasterContext;
import cgl.iotcloud.core.master.MasterSensorEvent;
import cgl.iotcloud.core.master.SensorSiteDescriptor;
import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;
import cgl.iotcloud.core.sensorsite.SensorEventState;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class MasterAPIServiceHandler implements TMasterAPIService.Iface {
    private static Logger LOG = LoggerFactory.getLogger(MasterAPIServiceHandler.class);

    private MasterContext masterContext;

    private BlockingQueue<MasterSensorEvent> sensorEvents;

    public MasterAPIServiceHandler(MasterContext masterContext, BlockingQueue<MasterSensorEvent> sensorEvents) {
        this.masterContext = masterContext;
        this.sensorEvents = sensorEvents;
    }

    @Override
    public List<String> getSites() throws TException {
        return new ArrayList<String>(masterContext.getSensorSites().keySet());
    }

    @Override
    public TSiteDetailsResponse getSite(String siteId) throws TException {
        SensorSiteDescriptor descriptor = masterContext.getSensorSite(siteId);

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
        masterContext.addSensorToDeploy(deployDescriptor);

        MasterSensorEvent event = new MasterSensorEvent(null, SensorEventState.DEPLOY);
        try {
            sensorEvents.put(event);
        } catch (InterruptedException e) {
            masterContext.removeSensorDeploy(deployDescriptor);
            LOG.error("Failed to add the new site..");
        }

        return new TResponse(TResponseState.SUCCESS, "success");
    }

    @Override
    public TResponse startSensor(List<String> sites, TSensorId id) throws TException {
        return null;
    }

    @Override
    public TResponse stopSensor(List<String> sites, TSensorId id) throws TException {
        return null;
    }

    @Override
    public List<TSensorDetails> getSensors(String siteId) throws TException {
        return null;
    }
}
