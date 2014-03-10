package cgl.iotcloud.core.transport.jms;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.Configurator;
import cgl.iotcloud.core.SensorContext;
import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.sensorsite.SiteContext;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.IdentityConverter;

import javax.jms.Message;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

public class JMSSensorConfigurator implements Configurator {
    private List<String> listeners = new ArrayList<String>();

    private List<String> senders = new ArrayList<String>();

    private String transportName = "jms";

    private SensorId id;

    public JMSSensorConfigurator(SensorId id) {
        this.id = id;
    }

    public void setTransportName(String transportName) {
        this.transportName = transportName;
    }

    public void addListener(String listener) {
        listeners.add(listener);
    }

    public void addSender(String sender) {
        senders.add(sender);
    }

    @Override
    public SensorContext configure(SiteContext siteContext) {
        SensorContext sensorContext = new SensorContext(id);

        for (String listener : listeners) {
            Map properties = new HashMap();
            properties.put(Configuration.CHANNEL_JMS_DESTINATION, listener);
            properties.put(Configuration.CHANNEL_JMS_IS_QUEUE, "true");

            Channel listeningChannel = new Channel<Message, Message>(Channel.Direction.IN, properties,
                    new ArrayBlockingQueue<Message>(1000), new ArrayBlockingQueue<Message>(1000));
            listeningChannel.setConverter(new IdentityConverter());

            if (siteContext.getTransport(transportName) != null) {
                sensorContext.addChannel(transportName, listeningChannel);
            }
        }

        for (String sender : senders) {
            Map properties = new HashMap();
            properties.put(Configuration.CHANNEL_JMS_DESTINATION, sender);
            properties.put(Configuration.CHANNEL_JMS_IS_QUEUE, "true");

            Channel sendingChannel = new Channel<Message, Message>(Channel.Direction.IN, properties,
                    new ArrayBlockingQueue<Message>(1000), new ArrayBlockingQueue<Message>(1000));
            sendingChannel.setConverter(new IdentityConverter());

            if (siteContext.getTransport(transportName) != null) {
                sensorContext.addChannel(transportName, sendingChannel);
            }
        }

        return sensorContext;
    }
}
