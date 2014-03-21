package cgl.iotcloud.examples.chat;

import cgl.iotcloud.core.Utils;
import cgl.iotcloud.core.client.SensorClient;
import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;
import org.apache.thrift.transport.TTransportException;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // read the configuration file
        Map conf = Utils.readConfig();

        SensorClient client = null;
        try {
            client = new SensorClient(conf);

            List<String> sites = client.getSensorSites();

            SensorDeployDescriptor deployDescriptor = new SensorDeployDescriptor("file:///home/supun/dev/projects/iotcloud2/lib/iotcloud-examples-1.0-SNAPSHOT.jar", "cgl.iotcloud.examples.chat.ChatSensor");
            deployDescriptor.addDeploySites(sites);

            client.deploySensor(deployDescriptor);
        } catch (TTransportException e) {
            e.printStackTrace();
        }
    }
}
