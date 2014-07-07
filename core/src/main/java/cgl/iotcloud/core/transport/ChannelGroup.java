package cgl.iotcloud.core.transport;

import org.apache.activemq.broker.Broker;
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
    protected Map<BrokerHost, BlockingQueue> consumerQueues =  new HashMap<BrokerHost, BlockingQueue>();

    protected Map<BrokerHost, BlockingQueue> producerQueues =  new HashMap<BrokerHost, BlockingQueue>();

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
            consumerQueues.put(brokerHost, new ArrayBlockingQueue(1024));
            producerQueues.put(brokerHost, new ArrayBlockingQueue(1024));
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

                if (!producers.containsKey(host)) {
                    manageable = transport.registerProducer(host, channel.getProperties(), producerQueues.get(host));
                    producers.put(host, manageable);

                    ProducingWorker worker = new ProducingWorker(host, channel);
                    producingWorkers.put(host, worker);

                    Thread thread = new Thread(worker);
                    thread.start();

                    manageable.start();
                }

                // now register the channel with the brokers map
                // check weather you have a sender consumer for this host
                List<Channel> channels = brokerHostToProducerChannelMap.get(host);
                channels.add(channel);

                LOG.info("Registering channel {} with group {} and host {}", channel.getName(), name, host.toString());
                incrementProducerIndex();

                return host;

            } else if (channel.getDirection() == Direction.IN) {
                BrokerHost host = brokerHosts.get(consumerIndex);

                if (!consumers.containsKey(host)) {
                    manageable = transport.registerConsumer(host, channel.getProperties(), consumerQueues.get(host));
                    consumers.put(host, manageable);

                    ConsumingWorker worker = new ConsumingWorker(host, channel);
                    consumingWorkers.put(host, worker);

                    Thread thread = new Thread(worker);
                    thread.start();

                    manageable.start();
                }

                // now register the channel with the brokers map
                // check weather you have a sender consumer for this host
                List<Channel> channels = brokerHostToConsumerChannelMap.get(host);
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

    private class ProducingWorker implements Runnable {
        private BrokerHost host;

        private Channel channel;

        public ProducingWorker(BrokerHost host, Channel channel) {
            this.host = host;
            this.channel = channel;
        }

        @Override
        public void run() {
            while (run) {
                BlockingQueue transportQueue = producerQueues.get(host);


            }
        }
    }

    private class ConsumingWorker implements Runnable {
        private BrokerHost host;

        private Channel channel;

        private ConsumingWorker(BrokerHost host, Channel channel) {
            this.host = host;
            this.channel = channel;
        }

        @Override
        public void run() {
            while (run) {

            }
        }
    }
}
