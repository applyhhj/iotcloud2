package cgl.iotcloud.core;

import cgl.iotcloud.core.sensorsite.SiteContext;

public interface Configurator {
    SensorContext configure(SiteContext siteContext);
}
