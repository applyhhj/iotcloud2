package cgl.iotcloud.core.zk;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.master.MasterContext;
import cgl.iotcloud.core.utils.SerializationUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterPersistant {
    private static Logger LOG = LoggerFactory.getLogger(MasterPersistant.class);

    private CuratorFramework curatorFramework;

    private MasterContext context;

    public MasterPersistant(MasterContext context) {
        this.context = context;

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.curatorFramework = CuratorFrameworkFactory.newClient(Configuration.getZkConnectionString(context.getConf()), retryPolicy);
    }

    public void start() {
        this.curatorFramework.start();

        this.load();
    }

    public void load() {
        // this will create the given ZNode with the given data
        try {
            if (curatorFramework.checkExists().forPath(context.getParentPath()) == null) {
                curatorFramework.create().forPath(context.getParentPath());
            }
        } catch (Exception e) {
            String msg = "Failed to load iot: " + context.getParentPath() + " in ZK";
            LOG.error(msg, e);
        }
    }
}
