package cgl.iotcloud.core;

import java.util.Map;

public class Configuration {
    public static final String IOT_MASTER_SERVER_HOST = "iot.master.host";
    public static final String IOT_MASTER_SERVER_PORT = "iot.master.server.port";
    public static final String IOT_MASTER_API_PORT = "iot.master.api.port";
    public static final String IOT_MASTER_API_THREADS = "iot.master.api.threads";
    public static final String IOT_MASTER_SERVER_THREADS = "iot.master.server.threads";

    public static final String IOT_SENSORSITE_PORT = "iot.sensorsite.port";
    public static final String IOT_SENSORSITE_THREADS = "iot.sensorsite.threads";
    public static final String IOT_SENSORSITE_HOST = "iot.sensorsite.host";

    // the maximum number of sensor sites possible
    public static final String IOT_SENSOR_SITES_MAX = "iot.sensorsites.max";

    // transport specific configurations
    // this map holds the available transports
    public static final String IOT_SENSORSITE_TRANSPORTS = "iot.sensorsite.transports";
    public static final String IOT_SENSORSITE_TRANSPORT_CLASS = "class";

    // jms transport specific configurations
    public static final String IOT_SENSOR_SITE_CONFAC_JNDI_NAME = "jms.ConnectionFactoryJNDIName";

    // the name of the channel destination
    public static final String CHANNEL_JMS_DESTINATION = "destination";

    public static final String CHANNEL_JMS_IS_QUEUE = "isQueue";

    public static String getMasterHost(Map conf) {
        return (String) conf.get(IOT_MASTER_SERVER_HOST);
    }

    public static int getMasterServerPort(Map conf) {
        return (Integer) conf.get(IOT_MASTER_SERVER_PORT);
    }

    public static int getMasterServerThreads(Map conf) {
        return (Integer) conf.get(IOT_MASTER_SERVER_THREADS);
    }

    public static int getMasterAPIPort(Map conf) {
        return (Integer)conf.get(IOT_MASTER_API_PORT);
    }

    public static int getMasterAPIThreads(Map conf) {
        return (Integer)conf.get(IOT_MASTER_API_THREADS);
    }

    public static int getSensorSitePort(Map conf) {
        return (Integer) conf.get(IOT_SENSORSITE_PORT);
    }

    public static String getSensorSiteHost(Map conf) {
        return (String) conf.get(IOT_SENSORSITE_HOST);
    }

    public static int getSensorSiteThreads(Map conf) {
        return (Integer) conf.get(IOT_SENSORSITE_THREADS);
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

    public static int getIotSensorSitesMax(Map conf) {
        return (Integer) conf.get(IOT_SENSOR_SITES_MAX);
    }
}
