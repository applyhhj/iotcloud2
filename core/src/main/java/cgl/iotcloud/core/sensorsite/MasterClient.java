package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.SensorContext;
import cgl.iotcloud.core.api.thrift.*;
import cgl.iotcloud.core.desc.ChannelDescriptor;
import cgl.iotcloud.core.desc.SensorDescriptor;
import cgl.iotcloud.core.master.thrift.TMasterService;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
import cgl.iotcloud.core.utils.SensorUtils;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.List;
import java.util.Map;

public class MasterClient {
    private TMasterService.Client client;

    private TTransport transport;

    public MasterClient(String host, int port) throws TTransportException {
        TSocket socket = new TSocket(host, port);
        transport = new TFramedTransport(socket);
        TProtocol protocol = new TBinaryProtocol(transport);
        this.client = new TMasterService.Client(protocol);

        transport.open();
    }

    public boolean registerSite(String siteId, String siteHost, int sitePort) throws TException {
        TResponse response = client.registerSite(new TSiteDetails(siteId, sitePort, siteHost));
        return response.getState() == TResponseState.SUCCESS;
    }

    public void registerSensor(String siteId, SensorInstance sensor) throws TException {
        SensorContext context = sensor.getSensorContext();

        TSensorId tSensorId = new TSensorId(context.getId().getName(), context.getId().getName());
        TSensor tSensor = new TSensor();
        tSensor.setId(tSensorId);

        for (Map.Entry<String, List<Channel>> e: context.getChannels().entrySet()) {
            List<Channel> channels = e.getValue();
            String transport = e.getKey();

            for (Channel c : channels) {
                if (c.getDirection() == Direction.IN) {
                    // todo
                    TChannel tChannel = new TChannel(transport, TDirection.IN);
                    tSensor.addToChannels(tChannel);
                }
            }
        }

        client.registerSensor(siteId, tSensor);
    }

    public void unRegisterSensor(String siteId, SensorInstance sensor) throws TException {
        SensorContext context = sensor.getSensorContext();

        TSensorId tSensorId = new TSensorId(context.getId().getName(), context.getId().getName());

        client.unRegisterSensor(siteId, tSensorId);
    }

    public void updateSensor(String siteId, SensorDescriptor sensor) throws TException {
        TSensorId tSensorId = new TSensorId(sensor.getSensorId().getName(), sensor.getSensorId().getName());
        TSensor tSensor = new TSensor();
        tSensor.setId(tSensorId);
        tSensor.setState(SensorUtils.getSensorState(sensor.getState()));

        for (Map.Entry<String, List<ChannelDescriptor>> e: sensor.getChannels().entrySet()) {
            List<ChannelDescriptor> channels = e.getValue();
            String transport = e.getKey();

            for (ChannelDescriptor c : channels) {
                if (c.getDirection() == Direction.IN) {
                    // TODO
                    TChannel tChannel = new TChannel(transport, TDirection.IN);
                    tSensor.addToChannels(tChannel);
                }
            }
        }

        client.updateSensor(siteId, tSensor);
    }

    public void close() {
        transport.close();
    }

}
