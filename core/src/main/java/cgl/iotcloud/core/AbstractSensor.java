package cgl.iotcloud.core;

import cgl.iotcloud.core.transport.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public abstract class AbstractSensor implements ISensor {
    private Map<String, QueueProducer> producers = new HashMap<String, QueueProducer>();

    private Map<String, QueueListener> listeners = new HashMap<String, QueueListener>();

    public void startSend(Channel channel, MessageSender sender, int interval) {
        QueueProducer producer = new QueueProducer(sender, interval, channel);
        producers.put(channel.getName(), producer);

        Thread t = new Thread(producer);
        t.start();
    }

    public void startSend(Channel channel, BlockingQueue messages) {
        QueueProducer producer = new QueueProducer(messages, channel);
        producers.put(channel.getName(), producer);

        Thread t = new Thread(producer);
        t.start();
    }

    public void startListen(Channel channel, MessageReceiver receiver) {
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
        private BlockingQueue queue = new ArrayBlockingQueue(1024);

        private MessageSender messageSender;

        private boolean run = true;

        private int interval;

        private boolean pause = false;

        private BlockingQueue messages;

        private Channel channel;

        private QueueProducer(MessageSender handler, int interval, Channel channel) {
            this.messageSender = handler;
            this.interval = interval;
            this.channel = channel;
        }

        public QueueProducer(BlockingQueue messages, Channel channel) {
            this.messages = messages;
            this.channel = channel;
        }

        @Override
        public void run() {
            if (messages == null) {
                runFixedInterval();
            } else {
                runFromQueue();
            }
        }

        private void runFixedInterval() {
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

                while (queue.isEmpty()) {
                    try {
                        Object data = queue.take();
                        channel.publish((byte [])data, null);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private void runFromQueue() {
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
                try {
                    Object message = messages.take();
                    channel.publish((byte [])message, null);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

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
    public void close() {
        // do nothing
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
