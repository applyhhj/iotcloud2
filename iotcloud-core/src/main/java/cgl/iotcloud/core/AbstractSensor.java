package cgl.iotcloud.core;

import cgl.iotcloud.core.transport.Channel;
import org.slf4j.Logger;

import java.util.concurrent.BlockingQueue;

public abstract class AbstractSensor {
    private final Logger Log;

    protected AbstractSensor(Logger log) {
        Log = log;
    }

    public void startChannel(Channel channel) {

    }

    private class QueueProducer implements Runnable {
        private BlockingQueue queue;

        private MessageSender messageSender;

        private QueueProducer(BlockingQueue queue, MessageSender handler) {
            this.queue = queue;
            this.messageSender = handler;
        }

        @Override
        public void run() {
            while (true) {
                messageSender.loop(queue);
            }
        }
    }

    private class QueueListener implements Runnable {
        private BlockingQueue queue;

        private MessageReceiver messageReceiver;

        private Logger logger;

        private QueueListener(BlockingQueue queue, MessageReceiver handler, Logger logger) {
            this.queue = queue;
            this.messageReceiver = handler;
            this.logger = logger;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Object o = queue.take();

                    messageReceiver.onMessage(o);
                } catch (InterruptedException e) {
                    logger.warn("Failed to retrieve the message");
                }
            }
        }
    }
}
