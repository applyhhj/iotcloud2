package cgl.iotcloud.core.api;

import cgl.iotcloud.core.api.thrift.*;
import cgl.iotcloud.core.master.MasterContext;
import cgl.iotcloud.core.master.SensorSiteDetails;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MasterAPIServiceHandler implements TMasterAPIService.Iface {
    private static Logger LOG = LoggerFactory.getLogger(MasterAPIServiceHandler.class);

    private MasterContext masterContext;

    public MasterAPIServiceHandler(MasterContext masterContext) {
        this.masterContext = masterContext;
    }

    @Override
    public List<String> getSites() throws TException {
        return new ArrayList<String>(masterContext.getSensorSites().keySet());
    }

    @Override
    public TSiteDetailsResponse getSite(String siteId) throws TException {
        SensorSiteDetails details = masterContext.getSensorSite(siteId);

        if (details != null) {
            TSiteDetails siteDetails = new TSiteDetails(siteId, details.getPort(), details.getHost());
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
    public TSensorDeployResponse deploySensor(List<String> sites, TSensorDetails sensor) throws TException {

        return null;
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
