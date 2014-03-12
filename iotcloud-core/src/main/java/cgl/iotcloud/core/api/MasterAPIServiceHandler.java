package cgl.iotcloud.core.api;

import cgl.iotcloud.core.api.thrift.*;
import org.apache.thrift.TException;

import java.util.List;

public class MasterAPIServiceHandler implements TMasterAPIService.Iface {

    @Override
    public TSiteDetailsResponse getSite(String siteId) throws TException {
        return null;
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
