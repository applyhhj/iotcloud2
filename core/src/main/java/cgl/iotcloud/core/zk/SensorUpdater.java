package cgl.iotcloud.core.zk;

import cgl.iotcloud.core.api.thrift.TSensor;
import cgl.iotcloud.core.api.thrift.TSensorId;
import cgl.iotcloud.core.api.thrift.TSite;
import cgl.iotcloud.core.desc.SensorDescriptor;
import cgl.iotcloud.core.master.MasterContext;
import cgl.iotcloud.core.utils.SerializationUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            if (client.checkExists().forPath(context.getParentPath() + "/" + site + "/" + SENSORS_NODE + descriptor.getId().getName()) == null) {
                client.create().withMode(CreateMode.PERSISTENT).forPath(context.getParentPath() + "/" + site + "/" + SENSORS_NODE + descriptor.getId().getName());
            }

            if (client.checkExists().forPath(context.getParentPath() + "/" + site + "/" + SENSORS_NODE + "/" + descriptor.getId().getName() + "/" + descriptor.getSensorId()) != null) {
                String msg = "The sensor: " + descriptor.getId().getName() + "is already deployed in the site:" + site + " with id: " + descriptor.getSensorId();
                LOG.error(msg);
                throw new RuntimeException("Failed to deploy sensor");
            }

            client.create().withMode(CreateMode.EPHEMERAL).forPath(
                    context.getParentPath() + "/" + site + "/" + SENSORS_NODE + descriptor.getId().getName() + "/" + descriptor.getSensorId(),
                            SerializationUtils.serializeThriftObject(descriptor));
        } catch (Exception e) {
            String msg = "Failed to register the sensor in ZK";
            LOG.error(msg, e);
        }
    }

    public static void removeSensor(CuratorFramework client, MasterContext context, String siteId, TSensorId id) {
        try {
            if (client.checkExists().forPath(context.getParentPath() + "/" + site + "/" + SENSORS_NODE + "/" + descriptor.getId().getName() + "/" + descriptor.getSensorId()) != null) {
                String msg = "The sensor: " + descriptor.getId().getName() + "is already deployed in the site:" + site + " with id: " + descriptor.getSensorId();
                LOG.error(msg);
                throw new RuntimeException("Failed to deploy sensor");
            }
            client.delete().forPath(getSensorPath(context.getParentPath(), id));
        } catch (Exception e) {
            String msg = "Failed to remove the sensor: " + getSensorPath(context.getParentPath(), id) + " from ZK";
            LOG.error(msg, e);
        }
    }

    private static String getSitePath(String parent, TSite descriptor) {
        return parent + "/" + descriptor.getSiteId();
    }

    private static String getSensorPath(String parent, SensorDescriptor descriptor) {
        return parent + "/" + descriptor.getSensorId().getGroup() + "/" + descriptor.getSensorId().getName();
    }

    private static String getSensorPath(String parent, TSensorId descriptor) {
        return parent + "/" + descriptor.getGroup() + "/" + descriptor.getName();
    }
}
