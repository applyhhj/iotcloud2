package cgl.iotcloud.core.transport;

import cgl.iotcloud.core.MessageReceiver;
import cgl.iotcloud.core.msg.TransportMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class ConsumingWorker implements Runnable {
    private static Logger LOG = LoggerFactory.getLogger(ConsumingWorker.class);

    private List<Channel> channels;

    private boolean run;

    private ExecutorService executorService;

    private BlockingQueue<TransportMessage> transportMessages;

    public ConsumingWorker(ExecutorService executorService, List<Channel> channels, BlockingQueue<TransportMessage> transportMessages) {
        this.executorService = executorService;
        this.transportMessages = transportMessages;
        this.channels = channels;
        this.run = true;
    }

    @Override
    public void run() {
        while (run) {
            try {
                TransportMessage message = transportMessages.take();
                // find the channel responsible for this message
                String sensorId = message.getSensorId();
                if (sensorId == null) {
                    throw new IllegalStateException("The message id of a transport message should be present");
                }

                Channel matchingChannel = null;
                for (Channel channel : channels) {
                    if (channel.getSensorID().equals(sensorId)) {
                        matchingChannel = channel;
                        break;
                    }
                }

                if (matchingChannel != null) {
                    MessageConverter converter = matchingChannel.getConverter();
                    Object convertedMessage = message;
                    if (converter != null) {
                        convertedMessage = converter.convert(transportMessages, null);
                    }

                    MessageReceiver receiver = matchingChannel.getReceiver();
                    if (receiver == null) {
                        String msg = "A receiving channel should specify a MessageReceiver";
                        LOG.error(msg);
                        throw new RuntimeException(msg);
                    }

                    if (executorService == null) {
                        receiver.onMessage(convertedMessage);
                    } else {
                        executorService.execute(new Work(receiver, convertedMessage));
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Failed to get the message from queue");
            }
        }
    }

    public void stop() {
        run = false;
    }

    private class Work implements Runnable {
        MessageReceiver receiver;

        Object message;

        private Work(MessageReceiver receiver, Object message) {
            this.receiver = receiver;
            this.message = message;
        }

        @Override
        public void run() {
            receiver.onMessage(message);
        }
    }
}
