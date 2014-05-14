package cgl.iotcloud.transport.kestrel;

import cgl.iotcloud.core.transport.MessageConverter;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class KestrelProducer {
    private Logger LOG = LoggerFactory.getLogger(KestrelProducer.class);

    private MessageConverter converter;

    private BlockingQueue outQueue;

    private KestrelThriftClient client;

    private int expirationTime = 30000;

    private boolean run = true;

    private KestrelDestination destination;

    private long blackListTime = 30000;

    private long sleepTime = 0;

    public KestrelProducer(KestrelDestination destination, BlockingQueue outQueue, MessageConverter converter) {
        this.destination = destination;
        this.outQueue = outQueue;
        this.converter = converter;
    }

    public void setExpirationTime(int expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void open() {
        Thread t = new Thread(new Worker());
        t.start();
    }

    public void close() {
        run = false;
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

    public void setBlackListTime(long blackListTime) {
        this.blackListTime = blackListTime;
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            int errorCount = 0;
            while (run) {
                try {
                    if (System.currentTimeMillis() > sleepTime) {
                        try {
                            getValidClient();
                        } catch (TException e) {
                            closeClient();
                            sleepTime = System.currentTimeMillis() + blackListTime;
                        }

                        try {
                            List<ByteBuffer> messages = new ArrayList<ByteBuffer>();
                            // get the number of items in the outQUeue
                            int size = outQueue.size();
                            if (size > 0) {
                                for (int i = 0; i < size; i++) {
                                    Object input = outQueue.take();
                                    Object converted = converter.convert(input, null);
                                    if (converted instanceof byte []) {
                                        ByteBuffer byteBuffer = ByteBuffer.allocate(((byte[]) converted).length);
                                        byteBuffer.put((byte[]) converted);
                                        messages.add(byteBuffer);
                                    } else {
                                        throw new RuntimeException("Expepected byte array after conversion");
                                    }
                                }
                            } else {
                                Object input = outQueue.take();
                                Object converted = converter.convert(input, null);
                                if (converted instanceof byte []) {
                                    ByteBuffer byteBuffer = ByteBuffer.allocate(((byte[]) converted).length);
                                    byteBuffer.put((byte[]) converted);
                                    messages.add(byteBuffer);
                                } else {
                                    throw new RuntimeException("Expepected byte array after conversion");
                                }
                            }
                            if (messages.size() > 0) {
                                try {
                                    client.put(destination.getQueue(), messages, expirationTime);
                                } catch (TException e) {
                                    closeClient();
                                    sleepTime = System.currentTimeMillis() + blackListTime;
                                }
                            }
                        } catch (InterruptedException e) {
                            LOG.error("Exception occurred in the worker listening for consumer changes", e);
                        }
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {
                        }
                    }
                } catch (Throwable t) {
                    errorCount++;
                    if (errorCount <= 3) {
                        LOG.error("Error occurred " + errorCount + " times.. trying to continue the worker");
                    } else {
                        LOG.error("Error occurred " + errorCount + " times.. terminating the worker");
                        run = false;
                    }
                }
            }
            String message = "Unexpected notification type";
            LOG.error(message);
            throw new RuntimeException(message);
        }
    }
}
