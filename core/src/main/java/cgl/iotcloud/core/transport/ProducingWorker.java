package cgl.iotcloud.core.transport;

import java.util.concurrent.BlockingQueue;

public class ProducingWorker implements Runnable {
    private BrokerHost host;

    private Channel channel;

    private boolean run;

    public ProducingWorker(BrokerHost host, Channel channel) {
        this.host = host;
        this.channel = channel;
    }

    @Override
    public void run() {
        while (run) {
        }
    }
}
