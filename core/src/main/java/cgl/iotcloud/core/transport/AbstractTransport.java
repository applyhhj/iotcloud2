package cgl.iotcloud.core.transport;

import cgl.iotcloud.core.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractTransport implements Transport {
    private static Logger LOG = LoggerFactory.getLogger(AbstractTransport.class);

    /**
     * Id of the site
     */
    protected String siteId;

    /**
     * This gateway is connected to these brokers
     */
    protected List<BrokerHost> brokerHosts;

    /**
     * Every transport has a list of applications. A group has specific channels registers to
     * it by the sensors
     */
    protected Map<String, ChannelGroup> groups = new ConcurrentHashMap<String, ChannelGroup>();

    /**
     * The transport specific configurations
     */
    protected Map transportConfiguration;

    protected ExecutorService executorService;

    @Override
    public void configure(String siteId, Map properties) {
        this.siteId = siteId;
        this.transportConfiguration = (Map)properties.get(Configuration.TRANSPORT_PROPERTIES);
        Object urlProp = transportConfiguration.get(TransportConstants.PROP_URLS);

        if (urlProp == null || !(urlProp instanceof List)) {
            String message = "Url is required by the Transport";
            LOG.error(message);
            throw new RuntimeException(message);
        }

        for (Object o : (List)urlProp) {
            if (o instanceof String) {
                String url = (String) o;
                brokerHosts.add(new BrokerHost(url));
            } else {
                LOG.error("Each broker URL should be a string");
                throw new RuntimeException("Each broker URL should be a string");
            }
        }

        Map threads = (Map) transportConfiguration.get(TransportConstants.THREAD_PROPERTY);
        if (threads != null) {
            int core = (Integer) threads.get(TransportConstants.CORE_PROPERTY);
            executorService = Executors.newScheduledThreadPool(core);
        }

        configureTransport();
    }

    public abstract void configureTransport();

    @Override
    public void registerChannel(ChannelName name, Channel channel) {
        // check to see if we already have a group for this channel
        ChannelGroup group = groups.get(channel.getGroup());
        if (group == null) {
            group = new ChannelGroup(channel.getName(), brokerHosts);
            groups.put(channel.getGroup(), group);
        }

        BrokerHost host = group.addChannel(channel);

        Map channelConf = channel.getProperties();
        if (channelConf == null) {
            String msg = "Channel properties must be specified";
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (channel.getDirection() == Direction.OUT) {
            registerProducer(host, channel.getProperties(), channel.getTransportQueue());
        } else if (channel.getDirection() == Direction.IN) {
            registerConsumer(host, channel.getProperties(), channel.getTransportQueue());
        }
    }

    public abstract Manageable registerProducer(BrokerHost host, Map channelConf, BlockingQueue queue);

    public abstract Manageable registerConsumer(BrokerHost host, Map channelConf, BlockingQueue queue);
}
