package cgl.iotcloud.core.transport;

public interface TransportConstants {
    public static final String PROP_URLS = "urls";

    public static final String THREAD_PROPERTY = "threads";
    public static final String CORE_PROPERTY = "core";
    public static final String MAX_PROPERTY = "max";

    // this property is used by some transports like rabbitmq to send the sensorID
    public static final String SENSOR_ID = "sensorID";

    public static final String TRANSPORT_RABBITMQ = "rabbitmq";
    public static final String TRANSPORT_KAFKA = "kafka";
}
