package cgl.iotcloud.core.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Contains a set of channels belonging to a particular group. The channels are
 * distributed across the available brokers. A Channel group consumes or produces messages from or
 * to a single logical queue.
 */
public class ChannelGroup {
    private static Logger LOG = LoggerFactory.getLogger(ChannelGroup.class);
    /**
     * Name of the group, this name is a unique combination of sensor group and channel name
     */
    private ChannelGroupName name;

    /**
     * Keep track of the channels for a broker host
     */
    protected Map<BrokerHost, List<Channel>> brokerHostToChannelMap = new ConcurrentHashMap<BrokerHost, List<Channel>>();

    /**
     * The available brokers
     */
    private List<BrokerHost> brokerHosts;

    /**
     * The index is used to pick the next broker available
     */
    private int currentIndex = 0;

    private Lock lock = new ReentrantLock();

    /**
     * These are the queues we put the messages coming from the channels. The actual message consumers or
     * senders use these queues
     */
    protected List<BlockingQueue> messageQueues =  new ArrayList<BlockingQueue>();

    protected AbstractTransport transport;

    protected Map<BrokerHost, Manageable> consumers = new HashMap<BrokerHost, Manageable>();

    protected Map<BrokerHost, Manageable> producers = new HashMap<BrokerHost, Manageable>();

    public ChannelGroup(ChannelGroupName name, List<BrokerHost> brokerHosts, AbstractTransport transport) {
        this.name = name;
        this.brokerHosts = brokerHosts;
        this.transport = transport;

        for (BrokerHost brokerHost : brokerHosts) {
            brokerHostToChannelMap.put(brokerHost, new ArrayList<Channel>());
            messageQueues.add(new ArrayBlockingQueue(1024));
        }
    }

    public BrokerHost addChannel(Channel channel) {
        lock.lock();
        try {
            // add the channel and return the broker host
            BrokerHost host = brokerHosts.get(currentIndex);

            Manageable manageable = null;
            if (channel.getDirection() == Direction.OUT) {
                manageable = transport.registerProducer(host, channel.getProperties(), channel.getTransportQueue());
                producers.put(host, manageable);
            } else if (channel.getDirection() == Direction.IN) {
                manageable = transport.registerConsumer(host, channel.getProperties(), channel.getTransportQueue());
                consumers.put(host, manageable);
            }

            // check weather you have a sender consumer for this host
            List<Channel> channels = brokerHostToChannelMap.get(host);
            channels.add(channel);

            // set the transport queue of the channel as the group queue
            channel.setTransportQueue(messageQueues.get(currentIndex));

            LOG.info("Registering channel {} with group {} and host {}", channel.getName(), name, host.toString());

            incrementIndex();

            return host;
        } finally {
            lock.unlock();
        }
    }

    private void incrementIndex() {
        if (currentIndex == brokerHosts.size() - 1) {
            currentIndex = 0;
        } else {
            currentIndex++;
        }
    }

    private class ProducingWorker implements Runnable {
        private Channel channel;

        private ProducingWorker(BlockingQueue queue) {
            //this.queue = queue;
        }

        @Override
        public void run() {
        }
    }

    private class ConsumingWorker implements Runnable {
        private BlockingQueue queue;

        @Override
        public void run() {

        }
    }
}
