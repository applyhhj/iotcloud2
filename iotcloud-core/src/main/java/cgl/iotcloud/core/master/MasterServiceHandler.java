package cgl.iotcloud.core.master;

import cgl.iotcloud.core.master.thrift.*;
import cgl.iotcloud.core.sensorsite.SiteContext;
import org.apache.thrift.TException;

public class MasterServiceHandler implements TMasterService.Iface {
    private SiteContext siteContext;

    public MasterServiceHandler(SiteContext siteContext) {
        this.siteContext = siteContext;
    }

    @Override
    public TRegisterSiteResponse registerSite(TRegisterSiteRequest request) throws TException {


        return null;
    }

    @Override
    public TResponse registerSensor(TSensor sensor) throws TException {
        return null;
    }

    @Override
    public TResponse unRegisterSensor(TSensorId sensor) throws TException {
        return null;
    }

    @Override
    public TResponse updateSensor(TSensor sensor) throws TException {
        return null;
    }
}
