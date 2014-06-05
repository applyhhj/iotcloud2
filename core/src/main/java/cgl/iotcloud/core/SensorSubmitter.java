package cgl.iotcloud.core;

import cgl.iotcloud.core.client.SensorClient;
import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class SensorSubmitter {
    private static Logger LOG = LoggerFactory.getLogger(SensorSubmitter.class);

    public static void submitSensor(Map<String, String> properties, String jarName, String className, List<String> sites) {
        Map conf = Utils.readConfig();
        SensorClient client = SensorClient.getConfiguredClient(conf);

        SensorDeployDescriptor deployDescriptor = new SensorDeployDescriptor(jarName, className);
        deployDescriptor.addDeploySites(sites);

        for (Map.Entry<String, String> e : properties.entrySet()) {
            deployDescriptor.addProperty(e.getKey(), e.getValue());
        }

        client.deploySensor(deployDescriptor);

        client.close();
    }

    public static void submitSensor(Map conf, Map<String, String> properties, String jarName, String className, List<String> sites) {
        SensorClient client = SensorClient.getConfiguredClient(conf);

        SensorDeployDescriptor deployDescriptor = new SensorDeployDescriptor(jarName, className);
        deployDescriptor.addDeploySites(sites);

        for (Map.Entry<String, String> e : properties.entrySet()) {
            deployDescriptor.addProperty(e.getKey(), e.getValue());
        }

        client.deploySensor(deployDescriptor);

        client.close();
    }

    /**
     * Submit the sensor according to the configuration given
     *
     * @param conf the storm conf
     */
    public static void submitSensor(Map conf, SensorDeployDescriptor sensorDeployDescriptor) {
        SensorClient client = SensorClient.getConfiguredClient(conf);

        client.deploySensor(sensorDeployDescriptor);

        client.close();
    }
}

