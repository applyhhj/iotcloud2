package cgl.iotcloud.examples.chat;

import cgl.iotcloud.core.*;
import cgl.iotcloud.core.sensorsite.SiteContext;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
import cgl.iotcloud.core.transport.MessageConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class RabbitMQSensor extends AbstractSensor {
    @Override
    public Configurator getConfigurator(Map conf) {
        return new RabbitConfigurator();
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

    private class RabbitConfigurator extends AbstractConfigurator {
        @Override
        public SensorContext configure(SiteContext siteContext) {
            SensorContext context = new SensorContext(new SensorId("rabbitChat", "general"));

            Map sendProps = new HashMap();
            sendProps.put("exchange", "test");
            sendProps.put("routingKey", "test1");
            Channel sendChannel = createChannel("sender", sendProps, Direction.OUT, 1024, new TextToByteConverter());

            Map receiveProps = new HashMap();
            receiveProps.put("queueName", "test");
            Channel receiveChannel = createChannel("receiver", receiveProps, Direction.OUT, 1024, new TextToByteConverter());

            context.addChannel("rabbitmq", sendChannel);
            context.addChannel("rabbitmq", receiveChannel);

            return context;
        }
    }

    private class ByteToTextConverter implements MessageConverter {
        @Override
        public Object convert(Object input, Object context) {
            return null;
        }
    }

    private class TextToByteConverter implements MessageConverter {

        @Override
        public Object convert(Object input, Object context) {
            return null;
        }
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
