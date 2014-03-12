package cgl.iotcloud.core.master;

import cgl.iotcloud.core.SensorId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterContext {
    private Map<String, SensorSiteDetails> sensorSiteDetails = new HashMap<String, SensorSiteDetails>();

    private Map<String, List<SensorDetails>> sensorDetails = new HashMap<String, List<SensorDetails>>();

    public void addSensorSite(SensorSiteDetails sensorSite) {
        sensorSiteDetails.put(sensorSite.getId(), sensorSite);
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

    public boolean removeSensor(String site, SensorId id) {
        if (!sensorSiteDetails.containsKey(site)) {
            return false;
        }
        List<SensorDetails> detailsList = sensorDetails.get(site);
        return detailsList != null && detailsList.remove(new SensorDetails(id));
    }
}
