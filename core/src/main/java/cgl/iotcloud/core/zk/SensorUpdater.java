package cgl.iotcloud.core.zk;

import cgl.iotcloud.core.api.thrift.TChannel;
import cgl.iotcloud.core.api.thrift.TSensor;
import cgl.iotcloud.core.api.thrift.TSite;
import cgl.iotcloud.core.master.MasterContext;
import cgl.iotcloud.core.utils.SerializationUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SensorUpdater {
    private static Logger LOG = LoggerFactory.getLogger(SensorUpdater.class);

    public static final String SENSORS_NODE = "sensors";

    public static void addSite(CuratorFramework client, String parent, TSite descriptor) {
        // this will create the given ZNode with the given data
        try {
            if (client.checkExists().forPath(parent + "/" + descriptor.getSiteId()) != null) {
                client.delete().forPath(parent + "/" + descriptor.getSiteId());
            }

            client.create().forPath(parent + "/" + descriptor.getSiteId(), SerializationUtils.serializeToBytes(descriptor));
        } catch (Exception e) {
            String msg = "Failed to register the site: " + getSitePath(parent, descriptor) + " in ZK";
            LOG.error(msg, e);
        }
    }

    public static void removeSite(CuratorFramework client, String path, TSite descriptor) {
        // this will create the given ZNode with the given data
        try {
            client.delete().forPath(getSitePath(path, descriptor));
        } catch (Exception e) {
            String msg = "Failed to remove the site: " + getSitePath(path, descriptor) + " from ZK";
            LOG.error(msg, e);
        }
    }

    public static void addSensor(CuratorFramework client, MasterContext context, String site, TSensor descriptor) {
        // this will create the given ZNode with the given data
        try {
            if (client.checkExists().forPath(context.getParentPath() + "/" + site + "/" + SENSORS_NODE) == null) {
                client.create().forPath(context.getParentPath() + "/" + site + "/" + SENSORS_NODE);
            }

            if (client.checkExists().forPath(context.getParentPath() + "/" + site + "/" + SENSORS_NODE + "/" + descriptor.getName()) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(context.getParentPath() + "/" + site + "/" + SENSORS_NODE + "/" + descriptor.getName());
            }

            if (client.checkExists().forPath(context.getParentPath() + "/" + site + "/" + SENSORS_NODE + "/" + descriptor.getName() + "/" + descriptor.getSensorId()) != null) {
                String msg = "The sensor: " + descriptor.getName() + " is already deployed in the site:" + site + " with id: " + descriptor.getSensorId();
                LOG.error(msg);
                throw new RuntimeException(msg);
            }

            client.create().withMode(CreateMode.PERSISTENT).forPath(
                    context.getParentPath() + "/" + site + "/" + SENSORS_NODE + "/" + descriptor.getName() + "/" + descriptor.getSensorId(),
                            SerializationUtils.serializeThriftObject(descriptor));

            // now get the channels for this sensor and add them to the zookeeper
            // for each channel we need
            /**
             * 1. transport
             * 2. properties
             * 3. broker url
             */
            for (TChannel channel : descriptor.getChannels()) {
                if (client.checkExists().forPath(context.getParentPath() + "/" + site + "/" + SENSORS_NODE + "/" + descriptor.getName() + "/" + descriptor.getSensorId() + "/" + channel.getName())  != null) {
                    String msg = "The channel: " + channel.getName() + " for sensor: " + descriptor.getName() + " is already registered in the site:" + site + " with id: " + descriptor.getSensorId() + "/" + channel.getName();
                    LOG.error(msg);
                    throw new RuntimeException(msg);
                }
                client.create().withMode(CreateMode.PERSISTENT).forPath(
                        context.getParentPath() + "/" + site + "/" + SENSORS_NODE + "/" + descriptor.getName() + "/" + descriptor.getSensorId() + "/" + channel.getName(),
                        SerializationUtils.serializeThriftObject(channel));
            }
        } catch (Exception e) {
            String msg = "Failed to register the sensor in ZK";
            LOG.error(msg, e);
        }
    }

    public static void removeSensor(CuratorFramework client, MasterContext context, String site, TSensor descriptor) {
        String path = context.getParentPath() + "/" + site + "/" + SENSORS_NODE + "/" + descriptor.getName() + "/" + descriptor.getSensorId();
        try {
            // delete all the channels for this
            List<String> clients = client.getChildren().forPath(context.getParentPath() + "/" + site + "/" + SENSORS_NODE + "/" + descriptor.getName() + "/" + descriptor.getSensorId());
            for (String cl : clients) {
                client.delete().forPath(context.getParentPath() + "/" + site + "/" + SENSORS_NODE + "/" + descriptor.getName() + "/" + descriptor.getSensorId() + "/" + cl);
            }

            if (client.checkExists().forPath(path) == null) {
                String msg = "The sensor: " + descriptor.getName() + " is not deployed in the site:" + site + " with id: " + descriptor.getSensorId();
                LOG.error(msg);
                throw new RuntimeException(msg);
            }

            client.delete().forPath(path);
        } catch (Exception e) {
            String msg = "Failed to remove the sensor: " + path + " from ZK";
            LOG.error(msg, e);
        }
    }

    public static void addChannel(CuratorFramework client, MasterContext context, String site, TChannel channel) {

    }

    private static String getSitePath(String parent, TSite descriptor) {
        return parent + "/" + descriptor.getSiteId();
    }
}
