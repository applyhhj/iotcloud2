package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.SensorContext;
import cgl.iotcloud.core.api.thrift.*;
import cgl.iotcloud.core.master.thrift.TMasterService;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
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
        TResponse response = client.registerSite(new TSite(siteId, sitePort, siteHost));
        return response.getState() == TResponseState.SUCCESS;
    }

    public void registerSensor(String siteId, SensorInstance sensor) throws TException {
        SensorContext context = sensor.getSensorContext();

        TSensor tSensor = createTSensor(context);
        tSensor.setState(TSensorState.DEPLOY);

        client.updateSensor(siteId, tSensor);
    }

    private TSensor createTSensor(SensorContext context) {
        String tSensorId = context.getName();
        TSensor tSensor = new TSensor();
        tSensor.setName(tSensorId);
        tSensor.setSensorId(context.getSensorID());

        for (Map.Entry<String, List<Channel>> e: context.getChannels().entrySet()) {
            List<Channel> channels = e.getValue();
            String transport = e.getKey();

            for (Channel c : channels) {
                TChannel tChannel;
                if (c.getDirection() == Direction.IN) {
                    // todo
                    tChannel = new TChannel(transport, TDirection.IN);

                } else {
                    tChannel = new TChannel(transport, TDirection.OUT);
                }
                for (Object key : c.getProperties().keySet()) {
                    tChannel.putToProperties(key.toString(), c.getProperties().get(key).toString());
                }
                tSensor.addToChannels(tChannel);
            }
        }
        return tSensor;
    }

    public void unRegisterSensor(String siteId, SensorInstance sensor) throws TException {
        SensorContext context = sensor.getSensorContext();
        TSensor tSensor = createTSensor(context);
        tSensor.setState(TSensorState.UN_DEPLOY);

        client.updateSensor(siteId, tSensor);
    }

    public void updateSensor(String siteId, SensorInstance sensor, SensorState state) throws TException {
        SensorContext context = sensor.getSensorContext();

        TSensor tSensor = createTSensor(context);

        if (state == SensorState.ACTIVATE) {
            tSensor.setState(TSensorState.ACTIVE);
        } else if (state == SensorState.DEACTIVATE) {
            tSensor.setState(TSensorState.DE_ACTIVATE);
        } else if (state == SensorState.UN_DEPLOY) {
            tSensor.setState(TSensorState.UN_DEPLOY);
        } else if (state == SensorState.DEPLOY) {
            tSensor.setState(TSensorState.DEPLOY);
        } else if (state == SensorState.UPDATE) {
            tSensor.setState(TSensorState.UPDATE);
        }

        client.updateSensor(siteId, tSensor);
    }

    public void close() {
        transport.close();
    }

}
