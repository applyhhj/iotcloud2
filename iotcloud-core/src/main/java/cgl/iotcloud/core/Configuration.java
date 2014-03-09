package cgl.iotcloud.core;

import java.util.Map;

public class Configuration {
    public static final String IOT_MASTER_SERVER_HOST = "iot.master.server.host";
    public static final String IOT_MASTER_SERVER_PORT = "iot.master.server.port";
    public static final String IOT_SENSORSITE_PORT = "iot.sensorsite.port";
    
    public static final String IOT_SENSORSITE_TRANSPORTS = "iot.sensorsite.transports";

    // jms transport specific configurations
    public static final String IOT_SENSOR_SITE_CONFAC_JNDI_NAME = "jms.ConnectionFactoryJNDIName";

    public static String getMasterServerHost(Map conf) {
        return (String) conf.get(IOT_MASTER_SERVER_HOST);
    }

    public static int getMasterServerPort(Map conf) {
        return (int) conf.get(IOT_MASTER_SERVER_PORT);
    }

    public static int getSensorSitePort(Map conf) {
        return (int) conf.get(IOT_SENSORSITE_PORT);
    }

    public static Map getTransports(Map conf) {
        return (Map) conf.get(IOT_SENSORSITE_TRANSPORTS);
    }
}
