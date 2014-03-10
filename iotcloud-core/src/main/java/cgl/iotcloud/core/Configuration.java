package cgl.iotcloud.core;

import java.util.Map;

public class Configuration {
    public static final String IOT_MASTER_SERVER_HOST = "iot.master.server.host";
    public static final String IOT_MASTER_SERVER_PORT = "iot.master.server.port";
    public static final String IOT_SENSORSITE_PORT = "iot.sensorsite.port";

    // transport specific configurations

    // this map holds the available transports
    public static final String IOT_SENSORSITE_TRANSPORTS = "iot.sensorsite.transports";
    public static final String IOT_SENSORSITE_TRANSPORT_CLASS = "class";

    // jms transport specific configurations
    public static final String IOT_SENSOR_SITE_CONFAC_JNDI_NAME = "jms.ConnectionFactoryJNDIName";

    // the name of the channel destination
    public static final String CHANNEL_JMS_DESTINATION = "destination";

    public static final String CHANNEL_JMS_IS_QUEUE = "isQueue";

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

    public static String getTransportClass(Map transportConf) {
        return (String) transportConf.get(IOT_SENSORSITE_TRANSPORT_CLASS);
    }

    public static String getChannelJmsDestination(Map channelConf) {
        return (String) channelConf.get(CHANNEL_JMS_DESTINATION);
    }

    public static String getChannelIsQueue(Map channelConf) {
        return (String) channelConf.get(CHANNEL_JMS_IS_QUEUE);
    }
}
