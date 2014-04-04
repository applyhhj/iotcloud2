package cgl.iotcloud.core.master;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterContext {
    private Map<String, SiteDescriptor> sites = new HashMap<String, SiteDescriptor>();

    private Map<String, List<SensorDetails>> siteSensors = new HashMap<String, List<SensorDetails>>();

    private Map<String, List<SensorDetails>> deactivatedSiteSensors = new HashMap<String, List<SensorDetails>>();

    private Map<String, SiteDescriptor> deactivatedSites = new HashMap<String, SiteDescriptor>();

    private List<SensorDeployDescriptor> sensorsTobeDeployed = new ArrayList<SensorDeployDescriptor>();

    public void addSensorSite(SiteDescriptor site) {
        sites.put(site.getId(), site);
    }

    public SiteDescriptor getSensorSite(String siteId) {
        return sites.get(siteId);
    }

    public Map<String, SiteDescriptor> getSensorSites() {
        return sites;
    }

    public boolean addSensor(String site, SensorDetails details) {
        if (!sites.containsKey(site)) {
            return false;
        }

        List<SensorDetails> detailsList = siteSensors.get(site);
        if (detailsList == null) {
            detailsList = new ArrayList<SensorDetails>();
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

    public boolean removeSensor(String site, SensorId id) {
        if (!sites.containsKey(site)) {
            return false;
        }
        List<SensorDetails> detailsList = siteSensors.get(site);
        return detailsList != null && detailsList.remove(new SensorDetails(id));
    }

    public void removeSite(String site) {
        siteSensors.remove(site);
        sites.remove(site);
    }

    public void addSensorToDeploy(SensorDeployDescriptor deployDescriptor) {
        sensorsTobeDeployed.add(deployDescriptor);
    }

    public void removeSensorDeploy(SensorDeployDescriptor deployDescriptor) {
        sensorsTobeDeployed.remove(deployDescriptor);
    }

    public List<SensorDeployDescriptor> getSensorsTobeDeployed() {
        return sensorsTobeDeployed;
    }
}
