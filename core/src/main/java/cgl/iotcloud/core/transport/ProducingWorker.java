package cgl.iotcloud.core.transport;

import cgl.iotcloud.core.msg.TransportMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class ProducingWorker implements Runnable {
    private static Logger LOG = LoggerFactory.getLogger(ProducingWorker.class);

    private Channel channel;

    private BlockingQueue<TransportMessage> transportMessages;

    private boolean run;

    public ProducingWorker(BrokerHost host, Channel channel) {
        this.channel = channel;
    }

    @Override
    public void run() {
        while (run) {
            try {
                TransportMessage transportMessage = transportMessages.take();


            } catch (InterruptedException e) {
                LOG.error("Failed to get the message from the queue");
            }
        }
    }
}
