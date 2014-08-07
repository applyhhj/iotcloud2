package cgl.iotcloud.core.utils;

import cgl.iotcloud.core.sensorsite.MasterClient;
import cgl.iotcloud.core.sensorsite.SiteContext;
import org.apache.thrift.transport.TTransportException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MasterClientCache {
    private int maxClients = 10;

    private BlockingQueue<MasterClient> siteClients = new ArrayBlockingQueue<MasterClient>(maxClients);

    private int count = 0;

    private SiteContext context;

    public MasterClientCache(SiteContext context) {
        this.context = context;
    }

    public MasterClient getMasterClient() throws Exception {
        if (siteClients.size() > 0 && count == 10) {
            return siteClients.poll();
        } else {
            MasterClient client = new MasterClient(context);
            count++;
            return client;
        }
    }

    public void done(MasterClient client) {
        siteClients.offer(client);
    }

    public void markClientFailed(MasterClient client) {
        if (client != null) {
            client.close();
        }
    }
}
