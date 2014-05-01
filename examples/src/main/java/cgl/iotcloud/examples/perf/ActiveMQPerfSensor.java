package cgl.iotcloud.examples.perf;

import cgl.iotcloud.core.*;
import cgl.iotcloud.core.client.SensorClient;
import cgl.iotcloud.core.msg.SensorTextMessage;
import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;
import cgl.iotcloud.core.sensorsite.SiteContext;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
import cgl.iotcloud.core.transport.MessageConverter;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                    queue.put(new SensorTextMessage("Hello"));
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
        public SensorContext configure(SiteContext siteContext, Map conf) {
            SensorContext context = new SensorContext(new SensorId("jms-perf", "perf"));

            Map sendProps = new HashMap();
            sendProps.put(Configuration.CHANNEL_JMS_IS_QUEUE, "false");
            sendProps.put(Configuration.CHANNEL_JMS_DESTINATION, "perf");

            Channel sendChannel = createChannel("sender", sendProps, Direction.OUT, 1024, new TextToJMSConverter());

            context.addChannel("jms", sendChannel);
            return context;
        }
    }

    private class TextToJMSConverter implements MessageConverter {
        @Override
        public Object convert(Object input, Object context) {
            if (context instanceof Session && input instanceof SensorTextMessage) {
                try {
                    TextMessage message = ((Session) context).createTextMessage();
                    message.setLongProperty("time", System.currentTimeMillis());
                    message.setText(((TextMessage) input).getText());

                    return message;
                } catch (JMSException e) {
                    LOG.error("Failed to convert SensorTextMessage to JMS message", e);
                }
            }
            return null;
        }
    }

    public static void main(String[] args) {
        // read the configuration file
        Map conf = Utils.readConfig();
        SensorClient client;
        try {
            client = new SensorClient(conf);

            List<String> sites = new ArrayList<String>();
            sites.add("local");

            SensorDeployDescriptor deployDescriptor = new SensorDeployDescriptor("iotcloud-examples-1.0-SNAPSHOT.jar", "cgl.iotcloud.examples.perf.ActiveMQPerfSensor");
            deployDescriptor.addDeploySites(sites);

            client.deploySensor(deployDescriptor);
        } catch (TTransportException e) {
            e.printStackTrace();
        }
    }
}
