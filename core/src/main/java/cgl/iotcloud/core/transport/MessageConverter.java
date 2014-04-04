package cgl.iotcloud.core.transport;

public interface MessageConverter {
    Object convert(Object input, Object context);
}
