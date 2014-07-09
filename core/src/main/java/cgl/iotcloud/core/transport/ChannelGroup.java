package cgl.iotcloud.core.transport;

import cgl.iotcloud.core.msg.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
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
    protected Map<BrokerHost, List<Channel>> brokerHostToProducerChannelMap = new ConcurrentHashMap<BrokerHost, List<Channel>>();

    protected Map<BrokerHost, List<Channel>> brokerHostToConsumerChannelMap = new ConcurrentHashMap<BrokerHost, List<Channel>>();

    /**
     * The available brokers
     */
    private List<BrokerHost> brokerHosts;

    /**
     * The index is used to pick the next broker available
     */
    private int consumerIndex = 0;

    private int producerIndex = 0;

    private Lock lock = new ReentrantLock();

    /**
     * These are the queues we put the messages coming from the channels. The actual message consumers or
     * senders use these queues
     */
    protected Map<BrokerHost, BlockingQueue<MessageContext>> consumerQueues =  new HashMap<BrokerHost, BlockingQueue<MessageContext>>();

    protected Map<BrokerHost, BlockingQueue<MessageContext>> producerQueues =  new HashMap<BrokerHost, BlockingQueue<MessageContext>>();

    protected AbstractTransport transport;

    protected Map<BrokerHost, Manageable> consumers = new HashMap<BrokerHost, Manageable>();

    protected Map<BrokerHost, Manageable> producers = new HashMap<BrokerHost, Manageable>();

    protected Map<BrokerHost, ProducingWorker> producingWorkers = new HashMap<BrokerHost, ProducingWorker>();

    protected Map<BrokerHost, ConsumingWorker> consumingWorkers = new HashMap<BrokerHost, ConsumingWorker>();

    protected boolean run;

    public ChannelGroup(ChannelGroupName name, List<BrokerHost> brokerHosts, AbstractTransport transport) {
        this.name = name;
        this.brokerHosts = brokerHosts;
        this.transport = transport;

        for (BrokerHost brokerHost : brokerHosts) {
            brokerHostToConsumerChannelMap.put(brokerHost, new ArrayList<Channel>());
            brokerHostToProducerChannelMap.put(brokerHost, new ArrayList<Channel>());
            consumerQueues.put(brokerHost, new ArrayBlockingQueue<MessageContext>(1024));
            producerQueues.put(brokerHost, new ArrayBlockingQueue<MessageContext>(1024));
        }

        this.run = true;
    }

    public BrokerHost addChannel(Channel channel) {
        lock.lock();
        try {
            // add the channel and return the broker host
            Manageable manageable;
            if (channel.getDirection() == Direction.OUT) {
                BrokerHost host = brokerHosts.get(producerIndex);
                List<Channel> channels = brokerHostToConsumerChannelMap.get(host);
                BlockingQueue<MessageContext> messageContexts = producerQueues.get(host);

                if (!producers.containsKey(host)) {
                    manageable = transport.registerProducer(host, channel.getProperties(), producerQueues.get(host));
                    producers.put(host, manageable);

                    manageable.start();
                }

                // now register the channel with the brokers map
                // check weather you have a sender consumer for this host
                channel.setOutQueue(messageContexts);
                channels.add(channel);

                LOG.info("Registering channel {} with group {} and host {}", channel.getName(), name, host.toString());
                incrementProducerIndex();

                return host;

            } else if (channel.getDirection() == Direction.IN) {
                BrokerHost host = brokerHosts.get(consumerIndex);
                List<Channel> channels = brokerHostToConsumerChannelMap.get(host);
                if (!consumers.containsKey(host)) {
                    BlockingQueue<MessageContext> messageContexts = consumerQueues.get(host);
                    manageable = transport.registerConsumer(host, channel.getProperties(), messageContexts);
                    consumers.put(host, manageable);

                    ConsumingWorker worker = new ConsumingWorker(channels, messageContexts);
                    consumingWorkers.put(host, worker);

                    Thread thread = new Thread(worker);
                    thread.start();

                    manageable.start();
                }

                // now register the channel with the brokers map
                // check weather you have a sender consumer for this host
                channels.add(channel);

                LOG.info("Registering channel {} with group {} and host {}", channel.getName(), name, host.toString());
                incrementConsumerIndex();

                return host;
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    private void incrementConsumerIndex() {
        if (consumerIndex == brokerHosts.size() - 1) {
            consumerIndex = 0;
        } else {
            consumerIndex++;
        }
    }

    private void incrementProducerIndex() {
        if (producerIndex == brokerHosts.size() - 1) {
            producerIndex = 0;
        } else {
            producerIndex++;
        }
    }

    public void start() {
        for (Manageable manageable : consumers.values()) {
            manageable.start();
        }

        for (Manageable manageable : producers.values()) {
            manageable.start();
        }
    }

    public void stop() {
        for (Manageable manageable : consumers.values()) {
            manageable.stop();
        }

        for (Manageable manageable : producers.values()) {
            manageable.stop();
        }
    }
}
