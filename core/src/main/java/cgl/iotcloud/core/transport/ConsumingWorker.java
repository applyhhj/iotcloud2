package cgl.iotcloud.core.transport;

public class ConsumingWorker implements Runnable {
    private BrokerHost host;

    private Channel channel;

    private boolean run;

    private ConsumingWorker(BrokerHost host, Channel channel) {
        this.host = host;
        this.channel = channel;
    }

    @Override
    public void run() {
        while (run) {

        }
    }
}
