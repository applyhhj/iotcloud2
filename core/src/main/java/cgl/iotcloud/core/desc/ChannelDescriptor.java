package cgl.iotcloud.core.desc;

import cgl.iotcloud.core.transport.Direction;

public class ChannelDescriptor {
    private Direction direction;

    public ChannelDescriptor(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}
