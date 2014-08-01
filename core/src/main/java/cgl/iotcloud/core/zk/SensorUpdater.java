package cgl.iotcloud.core.zk;

import cgl.iotcloud.core.api.thrift.TSensor;
import cgl.iotcloud.core.api.thrift.TSensorId;
import cgl.iotcloud.core.desc.SensorDescriptor;
import cgl.iotcloud.core.desc.SiteDescriptor;
import cgl.iotcloud.core.utils.SerializationUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorUpdater {
    private static Logger LOG = LoggerFactory.getLogger(SensorUpdater.class);

    public static void addSite(CuratorFramework client, String parent, SiteDescriptor descriptor) {
        // this will create the given ZNode with the given data
        try {
            if (client.checkExists().forPath(parent + "/" + descriptor.getId()) != null) {
                client.delete().forPath(parent + "/" + descriptor.getId());
            }

            client.create().forPath(parent + "/" + descriptor.getId(), SerializationUtils.serializeToBytes(descriptor));
        } catch (Exception e) {
            String msg = "Failed to register the site: " + getSitePath(parent, descriptor) + " in ZK";
            LOG.error(msg, e);
        }
    }

    public static void removeSite(CuratorFramework client, String path, SiteDescriptor descriptor) {
        // this will create the given ZNode with the given data
        try {
            client.delete().forPath(getSitePath(path, descriptor));
        } catch (Exception e) {
            String msg = "Failed to remove the site: " + getSitePath(path, descriptor) + " from ZK";
            LOG.error(msg, e);
        }
    }

    public static void addSensor(CuratorFramework client, String parent, TSensor descriptor) {
        // this will create the given ZNode with the given data
        try {
            client.create().withMode(CreateMode.PERSISTENT).forPath(getSensorPath(parent, descriptor.getId()), SerializationUtils.serializeThriftObject(descriptor));
        } catch (Exception e) {
            String msg = "Failed to register the sensor in ZK";
            LOG.error(msg, e);
        }
    }

    public static void removeSensor(CuratorFramework client, String parent, TSensorId id) {
        try {
            client.delete().forPath(getSensorPath(parent, id));
        } catch (Exception e) {
            String msg = "Failed to remove the sensor: " + getSensorPath(parent, id) + " from ZK";
            LOG.error(msg, e);
        }
    }

    private static String getSitePath(String parent, SiteDescriptor descriptor) {
        return parent + "/" + descriptor.getId();
    }

    private static String getSensorPath(String parent, SensorDescriptor descriptor) {
        return parent + "/" + descriptor.getSensorId().getGroup() + "/" + descriptor.getSensorId().getName();
    }

    private static String getSensorPath(String parent, TSensorId descriptor) {
        return parent + "/" + descriptor.getGroup() + "/" + descriptor.getName();
    }
}
