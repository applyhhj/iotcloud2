package cgl.sensorstream.core;

import cgl.sensorstream.core.config.Configuration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class ZooKeeperUpdater {
    private static Logger LOG = LoggerFactory.getLogger(ZooKeeperUpdater.class);

    private CuratorFramework client = null;

    private BlockingQueue<Update> updates;

    public void start(Map conf, BlockingQueue<Update> updates) {
        if (updates == null) {
            throw new IllegalArgumentException("Updates queue is required");
        }

        if (conf == null) {
            throw new IllegalArgumentException("Configuration map is required");
        }


        int retryTimes = (Integer) conf.get(Configuration.SS_ZOOKEEPER_RETRY_TIMES);
        int retryInterval = (Integer) conf.get(Configuration.SS_ZOOKEEPER_RETRY_INTERVAL);
        int maxSleepMs = (Integer) conf.get(Configuration.SS_ZOOKEEPER_RETRY_INTERVALCEILING_MILLIS);

        try {
            client = CuratorFrameworkFactory.newClient(Utils.getZkConnectionString(conf),
                    new ExponentialBackoffRetry(retryInterval, retryTimes, maxSleepMs));
            client.start();
        } catch (Exception e) {
            String msg = "Failed to create the connection to server" + Utils.getZkConnectionString(conf);
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public class Worker implements Runnable {
        Map conf;

        public Worker(Map conf) {
            this.conf = conf;
        }

        @Override
        public void run() {
            // make sure we don't die easily in case of error
            boolean run = true;
            int errorCount = 0;
            while (run) {
                try {
                    // take the update, block waiting if neccessary
                    Update update = updates.take();
                    // apply the update
                    processUpdate(conf, update);
                } catch (Throwable t) {
                    errorCount++;
                    if (errorCount <= 10) {
                        LOG.error("Error occurred " + errorCount + " times.. trying to continue the worker");
                    } else {
                        LOG.error("Error occurred " + errorCount + " times.. terminating the worker");
                        run = false;
                    }
                }
            }
            String message = "Unexpected notification type";
            LOG.error(message);
            throw new RuntimeException(message);
        }
    }

    private void processUpdate(Map conf, Update update) throws Exception {
        if (update.getType() == Update.Type.ADD) {
            try {
                client.create().forPath(getCompletePath(conf, update), update.getData());
            } catch (Exception e) {
                String msg = "Error creating the PATH";
                LOG.error(msg, e);
                throw new Exception(msg, e);
            }
        } else if (update.getType() == Update.Type.DELETE) {
            try {
                client.delete().forPath(getCompletePath(conf, update));
            } catch (Exception e) {
                String msg = "Error deleting the PATH";
                LOG.error(msg, e);
                throw new Exception(msg, e);
            }
        } else if (update.getType() == Update.Type.UPDATE) {
            try {
                client.setData().forPath(getCompletePath(conf, update), update.getData());
            } catch (Exception e) {
                String msg = "Error deleting the PATH";
                LOG.error(msg, e);
                throw new Exception(msg, e);
            }
        }
    }

    public void stop() {
        CloseableUtils.closeQuietly(client);
    }

    public static String getCompletePath(Map conf, Update update) {
        return conf.get(Configuration.SS_ZOOKEEPER_ROOT) + "/" + update.getPath();
    }
}
