package cgl.iotcloud.core;

import cgl.iotcloud.core.client.SensorClient;
import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SensorSubmitter {
    private static Logger LOG = LoggerFactory.getLogger(SensorSubmitter.class);

    /**
     * Submit the sensor according to the configuration given
     *
     * @param conf the storm conf
     */
    public static void submitSensor(Map conf, SensorDeployDescriptor sensorDeployDescriptor) {
        SensorClient client = SensorClient.getConfiguredClient(conf);

        client.deploySensor(sensorDeployDescriptor);
    }
}

