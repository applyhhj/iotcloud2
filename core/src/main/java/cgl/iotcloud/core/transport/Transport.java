package cgl.iotcloud.core.transport;

import java.util.Map;

public interface Transport {
    void configure(Map properties);

    void registerChannel(ChannelName name, Channel channel);

    void start();

    void stop();
}
