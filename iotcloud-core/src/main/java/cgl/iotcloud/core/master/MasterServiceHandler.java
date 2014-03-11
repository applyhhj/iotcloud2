package cgl.iotcloud.core.master;

import cgl.iotcloud.core.master.thrift.*;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterServiceHandler implements TMasterService.Iface {
    private static Logger LOG = LoggerFactory.getLogger(MasterServiceHandler.class);

    private MasterContext masterContext;

    public MasterServiceHandler(MasterContext masterContext) {
        this.masterContext = masterContext;
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