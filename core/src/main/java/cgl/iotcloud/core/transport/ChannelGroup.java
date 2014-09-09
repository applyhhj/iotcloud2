package cgl.iotcloud.core.transport;

import cgl.iotcloud.core.msg.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
    private String name;

    /**
     * Keep track of the channels for a broker host
     */
    protected Map<BrokerHost, List<Channel>> brokerHostToProducerChannelMap = new HashMap<BrokerHost, List<Channel>>();

    protected Map<BrokerHost, List<Channel>> brokerHostToConsumerChannelMap = new HashMap<BrokerHost, List<Channel>>();

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

    protected Map<BrokerHost, ConsumingWorker> consumingWorkers = new HashMap<BrokerHost, ConsumingWorker>();

    protected boolean run;

    protected String prefix;

    protected int brokerIndex;

    public ChannelGroup(String name, String prefix, List<BrokerHost> brokerHosts, AbstractTransport transport, int initialBrokerIndex) {
        this.name = name;
        this.brokerHosts = brokerHosts;
        this.transport = transport;
        this.prefix = prefix;
        this.brokerIndex = initialBrokerIndex;


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
                BrokerHost host = brokerHosts.get(brokerIndex);
                List<Channel> producerChannels = brokerHostToProducerChannelMap.get(host);
                BlockingQueue<MessageContext> channelOutQueue = producerQueues.get(host);

                if (!producers.containsKey(host)) {
                    manageable = transport.registerProducer(host, prefix, channel.getProperties(), producerQueues.get(host));
                    producers.put(host, manageable);

                    manageable.start();
                }

                // now register the channel with the brokers map
                // check weather you have a sender consumer for this host
                channel.setOutQueue(channelOutQueue);
                producerChannels.add(channel);
                producerChannels.add(new Channel("", Direction.OUT));

                LOG.info("Registering channel {} with group {} and host {}", channel.getName(), name, host.toString());
                incrementProducerIndex();

                return host;

            } else if (channel.getDirection() == Direction.IN) {
                BrokerHost host = brokerHosts.get(brokerIndex);
                List<Channel> consumerChannels = brokerHostToConsumerChannelMap.get(host);
                if (!consumers.containsKey(host)) {
                    BlockingQueue<MessageContext> channelInQueue = consumerQueues.get(host);
                    manageable = transport.registerConsumer(host, prefix, channel.getProperties(), channelInQueue);
                    consumers.put(host, manageable);

                    ConsumingWorker worker;
                    if (channel.isGrouped()) {
                        worker = new ConsumingWorker(consumerChannels, channelInQueue);
                    } else {
                        worker = new ConsumingWorker(consumerChannels, channelInQueue, true);
                    }
                    Thread thread = new Thread(worker);
                    thread.start();

                    manageable.start();

                    consumingWorkers.put(host, worker);
                }

                // now register the channel with the brokers map
                // check weather you have a sender consumer for this host
                consumerChannels.add(channel);

                LOG.info("Registering channel {} with group {} and host {}", channel.getName(), name, host.toString());
                incrementConsumerIndex();

                return host;
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    public void removeChannel(Channel channel) {
        lock.lock();
        try {
            if (channel.getDirection() == Direction.OUT) {
                BrokerHost registeredHost = null;
                for (Map.Entry<BrokerHost, List<Channel>> e : brokerHostToProducerChannelMap.entrySet()) {
                    List<Channel> channels = e.getValue();
                    Iterator<Channel> channelIterator = channels.iterator();
                    while (channelIterator.hasNext()) {
                        Channel c = channelIterator.next();
                        if (c.equals(channel)) {
                            registeredHost = e.getKey();
                            channelIterator.remove();

                            // if there are no more channels remove the producer
                            if (channels.size() == 0) {
                                Manageable producer = producers.remove(registeredHost);
                                producer.stop();
                            }
                            break;
                        }
                    }
                    if (registeredHost != null) {
                        break;
                    }
                }
            } else if (channel.getDirection() == Direction.IN) {
                BrokerHost registeredHost = null;
                for (Map.Entry<BrokerHost, List<Channel>> e : brokerHostToConsumerChannelMap.entrySet()) {
                    List<Channel> channels = e.getValue();
                    Iterator<Channel> channelIterator = channels.iterator();
                    while (channelIterator.hasNext()) {
                        Channel c = channelIterator.next();
                        if (c.equals(channel)) {
                            registeredHost = e.getKey();
                            channelIterator.remove();

                            // if there are no more channels remove the producer
                            if (channels.size() == 0) {
                                ConsumingWorker worker = consumingWorkers.remove(registeredHost);
                                worker.stop();

                                Manageable consumer = consumers.remove(registeredHost);
                                consumer.stop();
                            }
                            break;
                        }
                    }
                    if (registeredHost != null) {
                        break;
                    }
                }
            }
        } finally {
            lock.unlock();
        }
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

    public BrokerHost getHostForChannel(Channel channel) {
        BrokerHost registeredHost = null;
        lock.lock();
        try {
            if (channel.getDirection() == Direction.OUT) {
                for (Map.Entry<BrokerHost, List<Channel>> e : brokerHostToProducerChannelMap.entrySet()) {
                    List<Channel> channels = e.getValue();
                    Iterator<Channel> channelIterator = channels.iterator();
                    while (channelIterator.hasNext()) {
                        Channel c = channelIterator.next();
                        if (c.equals(channel)) {
                            registeredHost = e.getKey();
                            break;
                        }
                    }
                    if (registeredHost != null) {
                        break;
                    }
                }
            } else if (channel.getDirection() == Direction.IN) {
                for (Map.Entry<BrokerHost, List<Channel>> e : brokerHostToConsumerChannelMap.entrySet()) {
                    List<Channel> channels = e.getValue();
                    Iterator<Channel> channelIterator = channels.iterator();
                    while (channelIterator.hasNext()) {
                        Channel c = channelIterator.next();
                        if (c.equals(channel)) {
                            registeredHost = e.getKey();
                            break;
                        }
                    }
                    if (registeredHost != null) {
                        break;
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return registeredHost;
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
