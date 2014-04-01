package cgl.iotcloud.examples.chat;

import cgl.iotcloud.core.*;
import cgl.iotcloud.core.transport.Channel;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class RabbitMQSensor extends AbstractSensor {
    @Override
    public Configurator getConfigurator(Map conf) {
        return null;
    }

    @Override
    public void open(SensorContext context) {
        final Channel sendChannel = context.getChannel("rabbitmq", "sender");
        final Channel receiveChannel = context.getChannel("rabbitmq", "receiver");

        startChannel(sendChannel, new MessageSender() {
            @Override
            public boolean loop(BlockingQueue queue) {
                return false;
            }
        }, 100);

        startChannel(receiveChannel, new MessageReceiver() {
            @Override
            public void onMessage(Object message) {

            }
        });
    }

    @Override
    public void close() {

    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }
}
