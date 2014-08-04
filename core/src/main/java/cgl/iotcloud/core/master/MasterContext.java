package cgl.iotcloud.core.master;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.desc.SensorDescriptor;
import cgl.iotcloud.core.desc.SiteDescriptor;

import java.util.*;

public class MasterContext {
    private Map<String, SiteDescriptor> sites = new HashMap<String, SiteDescriptor>();

    private Map<String, List<SensorDescriptor>> siteSensors = new HashMap<String, List<SensorDescriptor>>();

    private Map<String, List<SensorDescriptor>> deactivatedSiteSensors = new HashMap<String, List<SensorDescriptor>>();

    private Map<String, SiteDescriptor> deactivatedSites = new HashMap<String, SiteDescriptor>();

    private Map conf;

    public MasterContext(Map conf) {
        this.conf = conf;
    }

    public void addSensorSite(SiteDescriptor site) {
        sites.put(site.getId(), site);
    }

    public SiteDescriptor getSensorSite(String siteId) {
        return sites.get(siteId);
    }

    public Map<String, SiteDescriptor> getSensorSites() {
        return sites;
    }

    public boolean addSensor(String site, SensorDescriptor details) {
        if (!sites.containsKey(site)) {
            return false;
        }

        List<SensorDescriptor> detailsList = siteSensors.get(site);
        if (detailsList == null) {
            detailsList = new ArrayList<SensorDescriptor>();
            siteSensors.put(site, detailsList);
        }

        detailsList.add(details);

        return true;
    }

    public void makeSiteOffline(String site) {
        if (!siteSensors.containsKey(site)) {
            SiteDescriptor siteDescriptor = sites.get(site);
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
        List<SensorDescriptor> detailsList = siteSensors.get(site);
        return detailsList != null && detailsList.remove(new SensorDescriptor(site, id));
    }

    public void removeSite(String site) {
        siteSensors.remove(site);
        sites.remove(site);
    }

    public SensorDescriptor getSensor(String siteId, String name) {
        List<SensorDescriptor> details = siteSensors.get(siteId);
        if (details != null) {
            for (SensorDescriptor detail : details) {
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
