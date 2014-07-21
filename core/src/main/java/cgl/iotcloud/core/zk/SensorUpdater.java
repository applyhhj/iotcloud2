package cgl.iotcloud.core.zk;

import cgl.iotcloud.core.desc.SiteDescriptor;
import cgl.iotcloud.core.sensor.SensorDescriptor;
import cgl.iotcloud.core.utils.SerializationUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorUpdater {
    private static Logger LOG = LoggerFactory.getLogger(SensorUpdater.class);

    public static void registerSite(CuratorFramework client, String parent, SiteDescriptor descriptor) {
        // this will create the given ZNode with the given data
        try {
            client.create().forPath(parent, SerializationUtils.serializeToBytes(descriptor));
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

    public static void registerSensor(CuratorFramework client, String parent, SensorDescriptor descriptor) {
        // this will create the given ZNode with the given data
        try {
            client.create().forPath(getSensorPath(parent, descriptor), SerializationUtils.serializeToBytes(descriptor));
        } catch (Exception e) {
            String msg = "Failed to register the sensor in ZK";
            LOG.error(msg, e);
        }
    }

    public static void removeSensor(CuratorFramework client, String parent, SensorDescriptor descriptor) {
        try {
            client.delete().forPath(getSensorPath(parent, descriptor));
        } catch (Exception e) {
            String msg = "Failed to remove the sensor: " + getSensorPath(parent, descriptor) + " from ZK";
            LOG.error(msg, e);
        }
    }

    private static String getSitePath(String parent, SiteDescriptor descriptor) {
        return parent + "/" + descriptor.getId();
    }

    private static String getSensorPath(String parent, SensorDescriptor descriptor) {
        return parent + "/" + descriptor.getSensorContext().getId().getGroup() + "/" + descriptor.getSensorContext().getId().getName();
    }
}
