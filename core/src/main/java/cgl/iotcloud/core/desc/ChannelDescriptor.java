package cgl.iotcloud.core.desc;

import cgl.iotcloud.core.transport.Direction;

import java.io.Serializable;

public class ChannelDescriptor implements Serializable {
    private Direction direction;

    public ChannelDescriptor(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }
}
