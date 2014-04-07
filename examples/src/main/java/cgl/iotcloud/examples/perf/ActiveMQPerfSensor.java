package cgl.iotcloud.examples.perf;

import cgl.iotcloud.core.*;
import cgl.iotcloud.core.sensorsite.SiteContext;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
import cgl.iotcloud.core.transport.MessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class ActiveMQPerfSensor extends AbstractSensor {
    private static Logger LOG = LoggerFactory.getLogger(ActiveMQPerfSensor.class);

    public Configurator getConfigurator(Map conf) {
        return new PerfConfigurator();
    }

    @Override
    public void open(SensorContext context) {
        final Channel sendChannel = context.getChannel("jms", "sender");

        startChannel(sendChannel, new MessageSender() {
            @Override
            public boolean loop(BlockingQueue queue) {
                try {
                    queue.put(new SensorBinaryMessage("Hello"));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }, 100);
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

    private class PerfConfigurator extends AbstractConfigurator {
        @Override
        public SensorContext configure(SiteContext siteContext) {
            SensorContext context = new SensorContext(new SensorId("jms-perf", "perf"));

            Map sendProps = new HashMap();
            sendProps.put(Configuration.CHANNEL_JMS_IS_QUEUE, "false");
            sendProps.put(Configuration.CHANNEL_JMS_DESTINATION, "perf");

            Channel sendChannel = createChannel("sender", sendProps, Direction.OUT, 1024, new BinaryToJMSConverter());

            context.addChannel("jms", sendChannel);
            return context;
        }
    }

    private class BinaryToJMSConverter implements MessageConverter {
        @Override
        public Object convert(Object input, Object context) {
            if (context instanceof Session && input instanceof SensorBinaryMessage) {
                try {
                    TextMessage message = ((Session) context).createTextMessage();
                    message.setLongProperty("time", System.currentTimeMillis());
                    message.setText(((SensorBinaryMessage) input).getText());

                    return message;
                } catch (JMSException e) {
                    LOG.error("Failed to convert SensorTextMessage to JMS message", e);
                }
            }
            return null;
        }
    }

    private class SensorBinaryMessage {
        String text;

        private SensorBinaryMessage(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}
