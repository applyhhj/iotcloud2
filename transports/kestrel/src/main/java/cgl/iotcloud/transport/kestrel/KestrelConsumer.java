package cgl.iotcloud.transport.kestrel;

import net.lag.kestrel.thrift.Item;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class KestrelConsumer {
    // by default we are going to black list a server for 30 secs
    private long blackListTime = 30000L;

    public static final int MAX_ITEMS = 64;

    public static final int WAIT_TIME = 10;

    private Logger logger;

    private KestrelThriftClient client = null;

    private BlockingQueue messages;

    private boolean run = true;

    private int timeoutMillis = 30000;

    private KestrelDestination destination;

    private long sleepTime = 0;

    public KestrelConsumer(Logger logger, KestrelDestination destination, BlockingQueue messages) {
        if (logger == null) {
            this.logger = LoggerFactory.getLogger(KestrelConsumer.class);
        }
        this.messages = messages;
        this.destination = destination;
    }

    public KestrelConsumer(KestrelDestination destination, BlockingQueue<KestrelMessage> messages) {
        this(null, destination, messages);
    }

    public void setTimeoutMillis(int timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public void setBlackListTime(long blackListTime) {
        this.blackListTime = blackListTime;
    }

    public void open() {
        Thread t = new Thread(new Worker());
        t.start();
    }

    public void ack(KestrelMessage message) {
        try {
            KestrelThriftClient thriftClient = getValidClient();
            Set<Long> set = new HashSet<Long>();
            set.add(message.getId());
            thriftClient.confirm(message.getQueue(), set);
        } catch (TException e) {
            logger.error("Failed to ack the message with id {}", message.getId(), e);
            closeClient();
        }
    }

    public void fail(KestrelMessage message) {
        try {
            KestrelThriftClient thriftClient = getValidClient();
            Set<Long> set = new HashSet<Long>();
            set.add(message.getId());
            thriftClient.abort(message.getQueue(), set);
        } catch (TException e) {
            logger.error("Failed to abort the message with id {}", message.getId(), e);
            closeClient();
        }
    }

    public void close() {
        run = false;
        closeClient();
    }

    private KestrelThriftClient getValidClient() throws TException {
        if (client == null) {
            client = new KestrelThriftClient(destination.getHost(), destination.getPort());
        }
        return client;
    }

    private void closeClient() {
        if (client != null) {
            client.close();
        }
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            while (run) {
                if (System.currentTimeMillis() > sleepTime) {
                    String q = destination.getQueue();
                    try {
                        getValidClient();
                    } catch (TException e) {
                        closeClient();
                        sleepTime = System.currentTimeMillis() + blackListTime;
                        continue;
                    }

                    List<Item> items;
                    try {
                        items = client.get(q, MAX_ITEMS, 0, 0);
                        if (items != null) {
                            for (Item item :items) {
                                byte[] bytes = item.get_data();
                                byte[] newBytes = Arrays.copyOf(bytes, bytes.length);
                                KestrelMessage m = new KestrelMessage(newBytes, item.get_id(), destination, q);
                                messages.put(m);
                            }
                        }
                    } catch (TException e) {
                        logger.debug("Error retrieving messages from queue {} and host {} port {}", q, destination.getHost(), destination.getPort());
                        closeClient();
                        sleepTime = System.currentTimeMillis() + blackListTime;
                    } catch (InterruptedException e) {
                        logger.error("Failed to add the message to the queue", e);
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }
}
