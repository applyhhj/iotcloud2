package cgl.sensorstream.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.management.resources.agent_pt_BR;

import java.util.Map;

public class Configuration {
    private static Logger LOG = LoggerFactory.getLogger(Configuration.class);

    // constants for configurations
    // the iot server url
    public static final String SS_IOT_SERVER = "localhost";
       
    // the zookeeper port
    public static final String SS_ZOOKEEPER_PORT = "storm.zookeeper.port";
    
    // the zookeeper host
    public static final String SS_ZOOKEEPER_SERVERS = "storm.zookeeper.servers";
    
    public static final String SS_ZOOKEEPER_ROOT = "ss.zookeeper.root";
    
    public static final String SS_ZOOKEEPER_SESSION_TIMEOUT = "ss.zookeeper.session.timeout";
    
    public static final String SS_ZOOKEEPER_CONNECTION_TIMEOUT = "ss.zookeeper.connection.timeout";
    
    public static final String SS_ZOOKEEPER_RETRY_TIMES = "ss.zookeeper.retry.times";

    public static final String SS_ZOOKEEPER_RETRY_INTERVAL = "ss.zookeeper.retry.interval";

    public static final String SS_ZOOKEEPER_RETRY_INTERVALCEILING_MILLIS = "ss.zookeeper.retry.intervalceiling.millis";

    // number of updates that we can handle at a given time
    public static final String SS_SENSOR_UPDATES_SIZE = "ss.sensor.updates.size";
    // the broker url to be used
    public static final String SS_BROKER_URL = "ss.broker.url";
    // the update listening queue
    public static final String SS_BROKER_UPDATE_QUEUE = "ss.broker.update.queue";
    
    public static final String SS_ZOOKEEPER_UPDATES_SIZE = "ss.zookeeper.updates.size";

    public static String getZkRoot(Map conf) {
        return (String) conf.get(SS_ZOOKEEPER_ROOT);
    }
}
