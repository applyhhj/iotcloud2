package cgl.iotcloud.core.transport;

import cgl.iotcloud.core.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

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
    protected Map<ChannelGroupName, ChannelGroup> groups = new ConcurrentHashMap<ChannelGroupName, ChannelGroup>();

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
            int max = (Integer) threads.get(TransportConstants.MAX_PROPERTY);
            executorService = new ThreadPoolExecutor(core, max, 5000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(1024));
        }

        configureTransport();
    }

    public abstract void configureTransport();

    @Override
    public void registerChannel(ChannelName name, Channel channel) {
        // check to see if we already have a group for this channel
        ChannelGroupName groupName = getGroupName(channel, name);
        boolean groupExists = groups.containsKey(groupName);

        ChannelGroup group = groups.get(groupName);
        if (group == null) {
            group = new ChannelGroup(groupName, brokerHosts);
            groups.put(groupName, group);
        }

        if (groupExists) {
            group.addChannel(channel);
        } else {
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
    }

    private ChannelGroupName getGroupName(Channel channel, ChannelName channelName) {
        return new ChannelGroupName(channel.getName(), channelName.getId().getGroup());
    }

    public abstract Manageable registerProducer(BrokerHost host, Map channelConf, BlockingQueue queue);

    public abstract Manageable registerConsumer(BrokerHost host, Map channelConf, BlockingQueue queue);
}
