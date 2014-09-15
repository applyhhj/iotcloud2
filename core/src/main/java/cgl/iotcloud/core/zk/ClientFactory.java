package cgl.iotcloud.core.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClientFactory {
    private Logger LOG = LoggerFactory.getLogger(ClientFactory.class);

    private int maxClients = 10;

    private List<CuratorFramework> curatorFrameworkList = new ArrayList<CuratorFramework>();

    private int currentClient = 0;

    private String address;

    private ClientFactory() {
    }

    private static ClientFactory instance = new ClientFactory();

    public static ClientFactory getInstance() {
        return instance;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setMaxClients(int maxClients) {
        this.maxClients = maxClients;
    }

    public synchronized CuratorFramework getClient() {
        LOG.info("current client {}, max client {}, size {}", currentClient, maxClients, curatorFrameworkList.size());
        if (curatorFrameworkList.size() < maxClients) {
            CuratorFramework client = CuratorFrameworkFactory.newClient(address, new ExponentialBackoffRetry(1000, 3));
            client.start();
            curatorFrameworkList.add(client);
        }
        CuratorFramework client = curatorFrameworkList.get(currentClient);
        if (currentClient < maxClients - 1) {
            currentClient++;
        } else {
            currentClient = 0;
        }

        return client;
    }

    public void close() {
        Iterator<CuratorFramework> client = curatorFrameworkList.iterator();
        while (client.hasNext()) {
            CuratorFramework curatorFramework = client.next();
            client.remove();
            CloseableUtils.closeQuietly(curatorFramework);
        }
    }
}