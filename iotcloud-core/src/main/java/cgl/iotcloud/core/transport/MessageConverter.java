package cgl.iotcloud.core.transport;

public interface MessageConverter <T, V>{
    V convert(T input);
}
