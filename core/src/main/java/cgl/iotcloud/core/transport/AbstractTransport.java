package cgl.iotcloud.core.transport;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.msg.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
    protected List<BrokerHost> brokerHosts = new ArrayList<BrokerHost>();

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
        ChannelGroup group = groups.get(groupName);
        if (group == null) {
            group = new ChannelGroup(groupName, brokerHosts, this);
            groups.put(groupName, group);
        }
        group.addChannel(channel);
    }

    /**
     * If the channel is grouped we will create a name with Channel name and sensor group
     * If the channel is not grouped, we will create a name with channel name and sensor ID which is unique,
     * this group will only have a single channel
     * @param channel channel
     * @param channelName channel name
     * @return a group name
     */
    protected ChannelGroupName getGroupName(Channel channel, ChannelName channelName) {
        if (channel.isGrouped()) {
            return new ChannelGroupName(channel.getName(), channelName.getId());
        } else {
            return new ChannelGroupName(channel.getName(), channel.getSensorID());
        }
    }

    public abstract Manageable registerProducer(BrokerHost host, Map channelConf, BlockingQueue<MessageContext> queue);

    public abstract Manageable registerConsumer(BrokerHost host, Map channelConf, BlockingQueue<MessageContext> queue);

    @Override
    public void start() {
        for (ChannelGroup group : groups.values()) {
            group.start();
        }
    }

    @Override
    public void stop() {
        for (ChannelGroup group : groups.values()) {
            group.stop();
        }
    }
}
