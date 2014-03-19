package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.SensorContext;
import cgl.iotcloud.core.api.thrift.*;
import cgl.iotcloud.core.master.thrift.TMasterService;
import cgl.iotcloud.core.master.thrift.TRegisterSiteRequest;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.util.List;
import java.util.Map;

public class MasterClient {
    private TMasterService.Client client;

    public MasterClient(String host, int port) {
        TTransport transport = new TSocket(host, port);
        TProtocol protocol = new TBinaryProtocol(transport);

        this.client = new TMasterService.Client(protocol);
    }

    public boolean registerSite(String siteId, String siteHost, int sitePort) throws TException {
        TResponse response = client.registerSite(new TRegisterSiteRequest(siteId, sitePort, siteHost));
        return response.getState() == TResponseState.SUCCESS;
    }

    public void registerSensor(String siteId, SensorDescriptor sensor) throws TException {
        SensorContext context = sensor.getSensorContext();

        TSensorId tSensorId = new TSensorId(context.getId().getName(), context.getId().getName());
        TSensor tSensor = new TSensor();
        tSensor.setId(tSensorId);

        for (Map.Entry<String, List<Channel>> e: context.getChannels().entrySet()) {
            List<Channel> channels = e.getValue();
            String transport = e.getKey();

            for (Channel c : channels) {
                if (c.getDirection() == Direction.IN) {
                    TChannel tChannel = new TChannel(transport, TDirection.IN);
                    tSensor.addToChannels(tChannel);
                }
            }
        }

        client.registerSensor(siteId, tSensor);
    }

    public void unRegisterSensor(String siteId, SensorDescriptor sensor) throws TException {
        SensorContext context = sensor.getSensorContext();

        TSensorId tSensorId = new TSensorId(context.getId().getName(), context.getId().getName());

        client.unRegisterSensor(siteId, tSensorId);
    }
}
