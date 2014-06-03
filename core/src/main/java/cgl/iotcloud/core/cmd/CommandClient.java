package cgl.iotcloud.core.cmd;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.Utils;
import cgl.iotcloud.core.client.SensorClient;
import org.apache.thrift.transport.TTransportException;

import java.util.Map;

public class CommandClient {
    // read the configuration file
    private Map conf = Utils.readConfig();

    private SensorClient client;

    public CommandClient() {
        try {
            client = new SensorClient(conf);
        } catch (TTransportException e) {
            System.out.println("Error occurred while trying to connect to sensor master");
        }
    }

    public void stopSensor(String name, String group) {
        client.stopSensor(new SensorId(name, group));
    }

    public void startSensor(String name, String group) {
        client.startSensor(new SensorId(name, group));
    }

    public static void main(String[] args) {

    }
}
