package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.Configurator;
import cgl.iotcloud.core.ISensor;
import cgl.iotcloud.core.SensorContext;
import cgl.iotcloud.core.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class SensorDeployer {
    public void deploySensor(Map conf, SiteContext siteContext, String jarName, String className) {
        try {
            ISensor sensor = Utils.loadSensor(new URL(jarName), className, this.getClass().getClassLoader());
            Configurator configurator = sensor.getConfigurator(conf);
            SensorContext sensorContext = configurator.configure(siteContext);

            siteContext.addSensor(sensorContext, sensor);
        } catch (MalformedURLException e) {
            throw new RuntimeException("The jar name is not a correct url");
        }
    }
}
