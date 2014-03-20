package cgl.iotcloud.examples.chat;

import cgl.iotcloud.core.*;
import cgl.iotcloud.core.sensorsite.SiteContext;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
import cgl.iotcloud.core.transport.MessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ChatSensor implements ISensor {
    private static Logger LOG = LoggerFactory.getLogger(ChatSensor.class);

    private SensorContext context;

    @Override
    public Configurator getConfigurator(Map conf) {
        return new ChatConfigurator();
    }

    @Override
    public void open(SensorContext context) {
        this.context = context;

        final Channel sendChannel = context.getChannel("jms", "sender");
        final Channel receiveChannel = context.getChannel("jms", "receive");

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        sendChannel.getInQueue().put(new SensorTextMessage("Hello"));
                    } catch (InterruptedException e) {

                    }
                }
            }
        });
        t1.start();

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Object o = receiveChannel.getOutQueue().take();
                    } catch (InterruptedException e) {

                    }
                }
            }
        });
        t2.start();

        LOG.info("Received open request {}", this.context.getId());
    }

    @Override
    public void close() {

    }

    @Override
    public void activate() {
        LOG.info("Received activation request {}", this.context.getId());
    }

    @Override
    public void deactivate() {
        LOG.info("Received de-activation request {}", this.context.getId());
    }

    private class ChatConfigurator implements Configurator {
        @Override
        public SensorContext configure(SiteContext siteContext) {
            SensorContext context = new SensorContext(new SensorId("chat", "general"));

            BlockingQueue<Message> inMassages = new ArrayBlockingQueue<Message>(1024);
            BlockingQueue<Message> outMassages = new ArrayBlockingQueue<Message>(1024);
            Map properties = new HashMap();
            properties.put(Configuration.CHANNEL_JMS_IS_QUEUE, "false");
            properties.put(Configuration.CHANNEL_JMS_DESTINATION, "receiver");
            Channel receiveChannel = new Channel("receive", Direction.IN, inMassages, outMassages, new JMSToTextMessageConverter());
            receiveChannel.addProperties(properties);
            context.addChannel("jms", receiveChannel);

            inMassages = new ArrayBlockingQueue<Message>(1024);
            outMassages = new ArrayBlockingQueue<Message>(1024);
            properties = new HashMap();
            properties.put(Configuration.CHANNEL_JMS_IS_QUEUE, "false");
            properties.put(Configuration.CHANNEL_JMS_DESTINATION, "sender");
            Channel sendChannel = new Channel("send", Direction.OUT, inMassages, outMassages, new TextToJMSMessageConverter());
            sendChannel.addProperties(properties);
            context.addChannel("jms", sendChannel);

            return context;
        }
    }

    private class JMSToTextMessageConverter implements MessageConverter {

        @Override
        public Object convert(Object input, Object context) {
            if (input instanceof TextMessage) {
                try {
                    SensorTextMessage message = new SensorTextMessage(((TextMessage) input).getText());
                } catch (JMSException e) {

                }
            }
            return null;
        }
    }

    private class TextToJMSMessageConverter implements MessageConverter {
        @Override
        public Object convert(Object input, Object context) {
            if (context instanceof Session && input instanceof SensorTextMessage) {
                try {
                    TextMessage message = ((Session) context).createTextMessage();
                    message.setText(((SensorTextMessage) input).getText());
                } catch (JMSException e) {

                }
            }
            return null;
        }
    }

    private class SensorTextMessage {
        String text;

        private SensorTextMessage(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}
