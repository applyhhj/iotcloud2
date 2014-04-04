package cgl.iotcloud.core.transport;

import java.util.Map;

public interface Transport {
    void configure(Map properties);

    void registerChannel(String name, Channel channel);

    void start();

    void stop();
}
