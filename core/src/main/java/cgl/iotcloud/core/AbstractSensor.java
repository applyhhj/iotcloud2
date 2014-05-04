package cgl.iotcloud.core;

import cgl.iotcloud.core.transport.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public abstract class AbstractSensor implements ISensor {
    private Map<String, QueueProducer> producers = new HashMap<String, QueueProducer>();

    private Map<String, QueueListener> listeners = new HashMap<String, QueueListener>();

    public void startChannel(Channel channel, MessageSender sender, int interval) {
        QueueProducer producer = new QueueProducer(channel.getInQueue(), sender, interval);
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

        private int interval;

        private boolean pause = false;

        private QueueProducer(BlockingQueue queue, MessageSender handler, int interval) {
            this.queue = queue;
            this.messageSender = handler;
            this.interval = interval;
        }

        @Override
        public void run() {
            while (run) {
                synchronized (this){
                    while (pause) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                messageSender.loop(queue);

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void stop() {
            run = false;
            notifyAll();
        }

        public void deactivate() {
            pause = true;
        }

        public synchronized void activate() {
            pause = false;
            notifyAll();
        }
    }

    protected class QueueListener implements Runnable {
        private BlockingQueue queue;

        private MessageReceiver messageReceiver;

        private boolean run = true;

        private boolean pause = false;

        private QueueListener(BlockingQueue queue, MessageReceiver handler) {
            this.queue = queue;
            this.messageReceiver = handler;
        }

        @Override
        public void run() {
            while (run) {
                synchronized (this) {
                    while (pause) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
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
            notifyAll();
        }

        public void deactivate() {
            pause = true;
        }

        public synchronized void activate() {
            pause = false;
            notifyAll();
        }
    }

    @Override
    public void activate() {
        for (QueueProducer p : producers.values()) {
            p.activate();
        }

        for (QueueListener p : listeners.values()) {
            p.activate();
        }
    }

    @Override
    public void deactivate() {
        for (QueueProducer p : producers.values()) {
            p.deactivate();
        }

        for (QueueListener p : listeners.values()) {
            p.deactivate();
        }
    }
}
