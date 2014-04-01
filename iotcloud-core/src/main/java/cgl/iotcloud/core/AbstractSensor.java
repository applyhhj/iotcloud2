package cgl.iotcloud.core;

import cgl.iotcloud.core.transport.Channel;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public abstract class AbstractSensor {
    private Map<String, QueueProducer> producers = new HashMap<String, QueueProducer>();

    private Map<String, QueueListener> listeners = new HashMap<String, QueueListener>();

    public void startChannel(Channel channel, MessageSender sender) {
        QueueProducer producer = new QueueProducer(channel.getInQueue(), sender);
        producers.put(channel.getName(), producer);

        Thread t = new Thread(producer);
        t.start();
    }

    public void startChannel(Channel channel, MessageReceiver receiver) {
        QueueListener listener = new QueueListener(channel.getOutQueue(), receiver);

        listeners.put(channel.getName(), listener);

        Thread t = new Thread(listener);
        t.start();
    }

    public void stopChannel(String name) {
        if (producers.containsKey(name)) {
            producers.get(name).stop();
        }

        if (listeners.containsKey(name)) {
            listeners.get(name).stop();
        }
    }

    protected class QueueProducer implements Runnable {
        private BlockingQueue queue;

        private MessageSender messageSender;

        private boolean run = true;

        private QueueProducer(BlockingQueue queue, MessageSender handler) {
            this.queue = queue;
            this.messageSender = handler;
        }

        @Override
        public void run() {
            while (run) {
                messageSender.loop(queue);
            }
        }

        public void stop() {
            run = false;
        }
    }

    protected class QueueListener implements Runnable {
        private BlockingQueue queue;

        private MessageReceiver messageReceiver;

        private Logger logger;
        private boolean run = true;

        private QueueListener(BlockingQueue queue, MessageReceiver handler) {
            this.queue = queue;
            this.messageReceiver = handler;
        }

        @Override
        public void run() {
            while (run) {
                try {
                    Object o = queue.take();

                    messageReceiver.onMessage(o);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void stop() {
            run = false;
        }
    }
}
