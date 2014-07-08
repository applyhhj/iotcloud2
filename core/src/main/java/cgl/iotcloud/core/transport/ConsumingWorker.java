package cgl.iotcloud.core.transport;

import cgl.iotcloud.core.msg.TransportMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ConsumingWorker implements Runnable {
    private static Logger LOG = LoggerFactory.getLogger(ConsumingWorker.class);

    private List<Channel> channels;

    private boolean run;

    private BlockingQueue<TransportMessage> transportMessages;

    public ConsumingWorker(List<Channel> channels, BlockingQueue<TransportMessage> transportMessages) {
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
                    String s = "The sensor id of a transport message should be present, discarding the message";
                    LOG.warn(s);
                    continue;
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

                    BlockingQueue receiver = matchingChannel.getOutQueue();
                    if (receiver == null) {
                        String msg = "A receiving channel should specify a MessageReceiver";
                        LOG.error(msg);
                        throw new RuntimeException(msg);
                    }

                    receiver.put(convertedMessage);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Failed to get the message from queue");
            }
        }
    }

    public void stop() {
        run = false;
    }
}
