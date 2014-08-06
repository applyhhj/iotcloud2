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


public class RabbitMQSensor extends AbstractSensor {
    private static Logger LOG = LoggerFactory.getLogger(RabbitMQSensor.class);

    private boolean run = true;
    @Override
    public Configurator getConfigurator(Map conf) {
        return new RabbitConfigurator();
    }

    @Override
    public void open(SensorContext context) {
        final Channel sendChannel = context.getChannel("rabbitmq", "sender");
        final Channel receiveChannel = context.getChannel("rabbitmq", "receiver");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (run) {
                    sendChannel.publish("Hello".getBytes());
                    try {
                        Thread.sleep(100);
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
            SensorContext context = new SensorContext("rabbitMQSensor");

            Map sendProps = new HashMap();
            sendProps.put("exchange", "test");
            sendProps.put("routingKey", "test");
            sendProps.put("queueName", "test");
            Channel sendChannel = createChannel("sender", sendProps, Direction.OUT, 1024);
            sendChannel.setGrouped(true);

            Map receiveProps = new HashMap();
            receiveProps.put("queueName", "test");
            Channel receiveChannel = createChannel("receiver", receiveProps, Direction.IN, 1024);
            receiveChannel.setGrouped(true);

            context.addChannel("rabbitmq", sendChannel);
            context.addChannel("rabbitmq", receiveChannel);

            return context;
        }
    }

    @Override
    public void close() {
        run = false;
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

        SensorDeployDescriptor deployDescriptor = new SensorDeployDescriptor("iotcloud-examples-1.0-SNAPSHOT.jar", "cgl.iotcloud.examples.chat.RabbitMQSensor");
        deployDescriptor.addDeploySites(sites);

        client.deploySensor(deployDescriptor);
    }
}
