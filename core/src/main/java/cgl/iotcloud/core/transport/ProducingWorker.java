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
        int errorCount = 0;
        while (run) {
            try {
                try {
                    Object input = channel.getInQueue().take();



                    channel.getOutQueue().put(input);
                } catch (InterruptedException e) {
                    LOG.error("Exception occurred in the worker listening for consumer changes", e);
                }
            } catch (Throwable t) {
                errorCount++;
                if (errorCount <= 3) {
                    LOG.error("Error occurred " + errorCount + " times.. trying to continue the worker", t);
                } else {
                    LOG.error("Error occurred " + errorCount + " times.. terminating the worker", t);
                    run = false;
                }
            }
        }
    }
}
