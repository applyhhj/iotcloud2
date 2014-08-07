package cgl.iotcloud.core.master;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.api.thrift.TSensor;
import cgl.iotcloud.core.api.thrift.TSite;

import java.util.*;

public class MasterContext {
    private Map<String, TSite> sites = new HashMap<String, TSite>();

    private Map<String, List<TSensor>> siteSensors = new HashMap<String, List<TSensor>>();

    private Map<String, List<TSensor>> deactivatedSiteSensors = new HashMap<String, List<TSensor>>();

    private Map<String, TSite> deactivatedSites = new HashMap<String, TSite>();

    private Map conf;

    public MasterContext(Map conf) {
        this.conf = conf;
    }

    public void addSensorSite(TSite site) {
        sites.put(site.getSiteId(), site);
    }

    public TSite getSensorSite(String siteId) {
        return sites.get(siteId);
    }

    public Map<String, TSite> getSensorSites() {
        return sites;
    }

    public boolean addSensor(String site, TSensor details) {
        if (!sites.containsKey(site)) {
            return false;
        }

        List<TSensor> detailsList = siteSensors.get(site);
        if (detailsList == null) {
            detailsList = new ArrayList<TSensor>();
            siteSensors.put(site, detailsList);
        }

        detailsList.add(details);

        return true;
    }

    public void makeSiteOffline(String site) {
        if (!siteSensors.containsKey(site)) {
            TSite siteDescriptor = sites.get(site);
            deactivatedSites.put(site, siteDescriptor);
            deactivatedSiteSensors.put(site, siteSensors.get(site));

            sites.remove(site);
            siteSensors.remove(site);
        }
    }

    public boolean removeSensor(String site, String id) {
        if (!sites.containsKey(site)) {
            return false;
        }
        List<TSensor> detailsList = siteSensors.get(site);
        Iterator<TSensor> itr = detailsList.iterator();
        while (itr.hasNext()) {
            TSensor sensor = itr.next();
            if (sensor.getSensorId().equals(id)) {
                itr.remove();
                return true;
            }
        }
        return false;
    }

    public void removeSite(String site) {
        siteSensors.remove(site);
        sites.remove(site);
    }

    public TSensor getSensor(String siteId, String name) {
        List<TSensor> details = siteSensors.get(siteId);
        if (details != null) {
            for (TSensor detail : details) {
                if (detail.getSensorId().equals(name)) {
                    return detail;
                }
            }
        }
        return null;
    }

    public Map getConf() {
        return conf;
    }

    public String getParentPath() {
        return Configuration.getZkRoot(conf);
    }
}
