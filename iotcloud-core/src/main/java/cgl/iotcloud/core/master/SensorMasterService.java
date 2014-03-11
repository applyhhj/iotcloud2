package cgl.iotcloud.core.master;

import cgl.iotcloud.core.master.thrift.*;
import org.apache.thrift.TException;

public class SensorMasterService implements TMasterService.Iface{
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
