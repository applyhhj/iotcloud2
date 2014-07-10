package cgl.iotcloud.core;

import cgl.iotcloud.core.msg.MessageContext;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class AbstractConfigurator implements Configurator {
    protected Channel createChannel(String name, Map properties,
                                    Direction direction, int queueSize) {

        BlockingQueue<MessageContext> inMassages = new ArrayBlockingQueue<MessageContext>(queueSize);
        BlockingQueue<MessageContext> outMassages = new ArrayBlockingQueue<MessageContext>(queueSize);

        Channel channel = new Channel(name, direction);
        channel.setInQueue(inMassages);
        channel.setOutQueue(outMassages);

        channel.addProperties(properties);

        return channel;
    }
}
