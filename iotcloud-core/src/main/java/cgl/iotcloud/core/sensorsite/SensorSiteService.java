package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.master.thrift.THeartBeatRequest;
import cgl.iotcloud.core.master.thrift.THeartBeatResponse;
import cgl.iotcloud.core.sensorsite.thrift.TSensorSiteService;
import org.apache.thrift.TException;

public class SensorSiteService implements TSensorSiteService.Iface {
    private SiteContext siteContext;

    public SensorSiteService(SiteContext siteContext) {
        this.siteContext = siteContext;
    }

    @Override
    public THeartBeatResponse hearbeat(THeartBeatRequest heartBeat) throws TException {
        THeartBeatResponse response = new THeartBeatResponse();
        response.setTotalSensors(siteContext.getRegisteredSensors().size());
        return response;
    }
}
