package cgl.iotcloud.core.sensor;

import cgl.iotcloud.core.transport.Direction;

public class ChannelDetails {
    private Direction direction;

    public ChannelDetails(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}
