package cgl.iotcloud.core.master;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterContext {
    private Map<String, SensorSiteDescriptor> sites = new HashMap<String, SensorSiteDescriptor>();

    private Map<String, List<SensorDetails>> siteSensors = new HashMap<String, List<SensorDetails>>();

    private Map<String, List<SensorDetails>> deactivatedSiteSensors = new HashMap<String, List<SensorDetails>>();

    private Map<String, SensorSiteDescriptor> deactivatedSites = new HashMap<String, SensorSiteDescriptor>();

    private List<SensorDeployDescriptor> sensorsTobeDeployed = new ArrayList<SensorDeployDescriptor>();

    public void addSensorSite(SensorSiteDescriptor sensorSite) {
        sites.put(sensorSite.getId(), sensorSite);
    }

    public SensorSiteDescriptor getSensorSite(String siteId) {
        return sites.get(siteId);
    }

    public Map<String, SensorSiteDescriptor> getSensorSites() {
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
            SensorSiteDescriptor sensorSiteDescriptor = sites.get(site);
            deactivatedSites.put(site, sensorSiteDescriptor);
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
}
