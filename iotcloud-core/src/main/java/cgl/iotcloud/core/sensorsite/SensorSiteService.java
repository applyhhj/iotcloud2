package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.api.thrift.TResponse;
import cgl.iotcloud.core.api.thrift.TSensorDeployResponse;
import cgl.iotcloud.core.api.thrift.TSensorDetails;
import cgl.iotcloud.core.api.thrift.TSensorId;
import cgl.iotcloud.core.master.thrift.THeartBeatRequest;
import cgl.iotcloud.core.master.thrift.THeartBeatResponse;
import cgl.iotcloud.core.sensorsite.thrift.TSensorSiteService;
import org.apache.thrift.TException;

import java.util.List;

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

    @Override
    public TSensorDeployResponse deploySensor(TSensorDetails sensor) throws TException {
        String className = sensor.getClassName();
        String jarName = sensor.getFilename();

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
}
