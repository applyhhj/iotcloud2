package cgl.iotcloud.examples.chat;

import cgl.iotcloud.core.*;
import cgl.iotcloud.core.client.SensorClient;
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

public class KafkaSensor extends AbstractSensor {
    private static Logger LOG = LoggerFactory.getLogger(KafkaSensor.class);

    @Override
    public Configurator getConfigurator(Map conf) {
        return new KafkaConfigurator();
    }

    @Override
    public void open(SensorContext context) {
        final Channel sendChannel = context.getChannel("kafka", "sender");
        final Channel receiveChannel = context.getChannel("kafka", "receiver");

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
                if (message instanceof byte[]) {
                    System.out.println(new String((byte[]) message));
                } else {
                    System.out.println("Unexpected message");
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private class KafkaConfigurator extends AbstractConfigurator {
        @Override
        public SensorContext configure(SiteContext siteContext, Map conf) {
            SensorContext context = new SensorContext(new SensorId("KafkaChat", "general"));

            Map sendProps = new HashMap();
            sendProps.put("topic", "test");
            sendProps.put("serializerClass", "kafka.serializer.DefaultEncoder");
            // sendProps.put("routingKey", "test1");
            // sendProps.put("queueName", "test");
            Channel sendChannel = createChannel("sender", sendProps, Direction.OUT, 1024);

            Map receiveProps = new HashMap();
            receiveProps.put("topic", "test");
            receiveProps.put("partition", 0);
            Channel receiveChannel = createChannel("receiver", receiveProps, Direction.IN, 1024);

            context.addChannel("kafka", sendChannel);
            context.addChannel("kafka", receiveChannel);

            return context;
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

    public static void main(String[] args) throws TTransportException {
        // read the configuration file
        Map conf = Utils.readConfig();
        SensorClient client;
        client = new SensorClient(conf);

        List<String> sites = new ArrayList<String>();
        sites.add("local");

        SensorDeployDescriptor deployDescriptor = new SensorDeployDescriptor("iotcloud-examples-1.0-SNAPSHOT.jar", "cgl.iotcloud.examples.chat.KafkaSensor");
        deployDescriptor.addDeploySites(sites);

        client.deploySensor(deployDescriptor);
    }
}
