package cgl.iotcloud.core;

import java.util.List;

public interface Sensor {
    SensorId getId();

    List<Channel> getChannels();


}
