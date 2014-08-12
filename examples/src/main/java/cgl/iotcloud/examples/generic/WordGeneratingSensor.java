package cgl.iotcloud.examples.generic;

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

import java.util.*;

public class WordGeneratingSensor extends AbstractSensor {
    private static Logger LOG = LoggerFactory.getLogger(WordGeneratingSensor.class);
    private boolean run = true;
    final String[] sentences = new String[]{ "the cow jumped over the moon", "an apple a day keeps the doctor away",
            "four score and seven years ago", "snow white and the seven dwarfs", "i am at two with nature" };

    @Override
    public Configurator getConfigurator(Map conf) {
        return new RabbitConfigurator();
    }

    @Override
    public void open(SensorContext context) {
        final Channel sendChannel = context.getChannel("rabbitmq", "sentence");
        final Channel receiveChannel = context.getChannel("rabbitmq", "count");
        final Random rand = new Random();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (run) {
                    String sentence = sentences[rand.nextInt(sentences.length)];
                    sendChannel.publish(sentence.getBytes());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();

        startListen(receiveChannel, new MessageReceiver() {
            @Override
            public void onMessage(Object message) {
                if (message instanceof MessageContext) {
                    System.out.println(new String(((MessageContext) message).getBody()));
                } else {
                    System.out.println("Unexpected message");
                }
            }
        });
        LOG.info("Starting the sensor...");
    }

    @SuppressWarnings("unchecked")
    private class RabbitConfigurator extends AbstractConfigurator {
        @Override
        public SensorContext configure(SiteContext siteContext, Map conf) {
            SensorContext context = new SensorContext("wordcount");

            Map sendProps = new HashMap();
            sendProps.put("exchange", "iot_examples");
            sendProps.put("routingKey", "sentence");
            sendProps.put("queueName", "sentence");
            Channel sendChannel = createChannel("sentence", sendProps, Direction.OUT, 1024);
            //sendChannel.setGrouped(true);

            Map receiveProps = new HashMap();
            receiveProps.put("queueName", "count");
            receiveProps.put("exchange", "iot_examples");
            receiveProps.put("routingKey", "count");
            Channel receiveChannel = createChannel("count", receiveProps, Direction.IN, 1024);
            //receiveChannel.setGrouped(true);

            context.addChannel("rabbitmq", sendChannel);
            context.addChannel("rabbitmq", receiveChannel);
            return context;
        }
    }

    @Override
    public void close() {
        run = false;
    }

    public static void main(String[] args) throws TTransportException {
        // read the configuration file
        Map conf = Utils.readConfig();
        SensorClient client;
        client = new SensorClient(conf);

        List<String> sites = new ArrayList<String>();
        sites.add("local");

        SensorDeployDescriptor deployDescriptor = new SensorDeployDescriptor("iotcloud-examples-1.0-SNAPSHOT.jar", "cgl.iotcloud.examples.generic.WordGeneratingSensor");
        deployDescriptor.addDeploySites(sites);

        client.deploySensor(deployDescriptor);
    }
}
