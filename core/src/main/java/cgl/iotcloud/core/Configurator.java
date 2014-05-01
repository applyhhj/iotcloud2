package cgl.iotcloud.core;

import cgl.iotcloud.core.sensorsite.SiteContext;

import java.util.Map;

public interface Configurator {
    SensorContext configure(SiteContext siteContext, Map conf);
}
