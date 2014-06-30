package cgl.iotcloud.core.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Contains a set of channels belonging to a particular group. The channels are
 * distributed across the available brokers.
 */
public class ChannelGroup {
    private static Logger LOG = LoggerFactory.getLogger(ChannelGroup.class);
    /**
     * Name of the group
     */
    private String name;

    /**
     * Keep track of the channels for a broker host
     */
    protected Map<BrokerHost, List<Channel>> brokerHostToChannelMap = new ConcurrentHashMap<BrokerHost, List<Channel>>();

    private List<BrokerHost> brokerHosts;

    private int currentIndex = 0;

    private Lock lock = new ReentrantLock();

    public ChannelGroup(String name, List<BrokerHost> brokerHosts) {
        this.name = name;
        this.brokerHosts = brokerHosts;

        for (BrokerHost brokerHost : brokerHosts) {
            brokerHostToChannelMap.put(brokerHost, new ArrayList<Channel>());
        }
    }

    public BrokerHost addChannel(Channel channel) {
        lock.lock();
            try {
            // add the channel and return the broker host
            BrokerHost host = brokerHosts.get(currentIndex);
            List<Channel> channels = brokerHostToChannelMap.get(host);
            channels.add(channel);

            LOG.info("Registering channel {} with group {} and host {}", channel.getName(), name, host.toString());

            incrementIndex();

            return host;
        } finally {
            lock.unlock();
        }
    }

    private void incrementIndex() {
        if (currentIndex <= brokerHosts.size() - 1) {
            currentIndex = 0;
        } else {
            currentIndex++;
        }
    }
}
