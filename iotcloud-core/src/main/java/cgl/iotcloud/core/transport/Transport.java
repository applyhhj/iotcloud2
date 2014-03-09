package cgl.iotcloud.core.transport;

import java.util.Map;

public interface Transport {
    void open(Map properties);

    void start();

    void stop();
}
