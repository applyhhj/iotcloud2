package cgl.iotcloud.core.master;

import cgl.iotcloud.core.SensorId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterContext {
    private Map<String, SensorSiteDetails> sensorSiteDetails = new HashMap<String, SensorSiteDetails>();

    private Map<String, List<SensorDetails>> sensorDetails = new HashMap<String, List<SensorDetails>>();

    private Map<String, List<SensorDetails>> offlineSites = new HashMap<String, List<SensorDetails>>();

    private Map<String, SensorDetails> deactivatedSensorSiteDetails = new HashMap<String, SensorDetails>();

    public void addSensorSite(SensorSiteDetails sensorSite) {
        sensorSiteDetails.put(sensorSite.getId(), sensorSite);
    }

    public SensorSiteDetails getSensorSite(String siteId) {
        return sensorSiteDetails.get(siteId);
    }

    public Map<String, SensorSiteDetails> getSensorSites() {
        return sensorSiteDetails;
    }

    public boolean addSensor(String site, SensorDetails details) {
        if (!sensorSiteDetails.containsKey(site)) {
            return false;
        }

        List<SensorDetails> detailsList = sensorDetails.get(site);
        if (detailsList == null) {
            detailsList = new ArrayList<SensorDetails>();
            sensorDetails.put(site, detailsList);
        }

        detailsList.add(details);

        return true;
    }

    public void makeSiteOffline(String site) {
        if (!sensorDetails.containsKey(site)) {

        }
    }

    public boolean removeSensor(String site, SensorId id) {
        if (!sensorSiteDetails.containsKey(site)) {
            return false;
        }
        List<SensorDetails> detailsList = sensorDetails.get(site);
        return detailsList != null && detailsList.remove(new SensorDetails(id));
    }
}
