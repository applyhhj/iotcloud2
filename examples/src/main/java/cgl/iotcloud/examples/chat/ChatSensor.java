package cgl.iotcloud.examples.chat;

import cgl.iotcloud.core.*;
import cgl.iotcloud.core.client.SensorClient;
import cgl.iotcloud.core.msg.MessageContext;
import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;
import cgl.iotcloud.core.sensorsite.SiteContext;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class ChatSensor extends AbstractSensor {
    private static Logger LOG = LoggerFactory.getLogger(ChatSensor.class);

    @Override
    public Configurator getConfigurator(Map conf) {
        return new ChatConfigurator();
    }

    @Override
    public void open(SensorContext context) {
        final Channel sendChannel = context.getChannel("jms", "sender");
        final Channel receiveChannel = context.getChannel("jms", "receiver");

        startSend(sendChannel, new MessageSender() {
            @Override
            public boolean loop(BlockingQueue<byte []> queue) {
                try {
                    queue.put("Hello".getBytes());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }, 100);

        startListen(receiveChannel, new MessageReceiver() {
            @Override
            public void onMessage(Object message) {
                if (message instanceof MessageContext) {
                    System.out.println(new String(((MessageContext) message).getBody()));
                }
            }
        });

        LOG.info("Received open request {}", context.getId());
    }

    @SuppressWarnings("unchecked")
    private class ChatConfigurator extends AbstractConfigurator {
        @Override
        public SensorContext configure(SiteContext siteContext, Map conf) {
            SensorContext context = new SensorContext(new SensorId("chat", "general"));

            Map properties = new HashMap();
            properties.put(Configuration.CHANNEL_JMS_IS_QUEUE, "false");
            properties.put(Configuration.CHANNEL_JMS_DESTINATION, "chat");
            Channel receiveChannel = createChannel("receiver", properties, Direction.IN, 1024);
            context.addChannel("jms", receiveChannel);

            properties = new HashMap();
            properties.put(Configuration.CHANNEL_JMS_IS_QUEUE, "false");
            properties.put(Configuration.CHANNEL_JMS_DESTINATION, "chat");
            Channel sendChannel = createChannel("sender", properties, Direction.OUT, 1024);
            context.addChannel("jms", sendChannel);

            return context;
        }
    }

    public static void main(String[] args) throws TTransportException {
        // read the configuration file
        Map conf = Utils.readConfig();
        SensorClient client;
        client = new SensorClient(conf);

        List<String> sites = new ArrayList<String>();
        sites.add("local");

        SensorDeployDescriptor deployDescriptor = new SensorDeployDescriptor("iotcloud-examples-1.0-SNAPSHOT.jar", "cgl.iotcloud.examples.chat.ChatSensor");
        deployDescriptor.addDeploySites(sites);

        client.deploySensor(deployDescriptor);
    }
}
