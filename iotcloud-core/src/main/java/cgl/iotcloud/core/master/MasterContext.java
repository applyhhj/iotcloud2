package cgl.iotcloud.core.master;

import java.util.ArrayList;
import java.util.List;

public class MasterContext {
    private List<SensorSiteDetails> sensorSiteDetails = new ArrayList<SensorSiteDetails>();

    public void addSensorSite(SensorSiteDetails sensorSite) {
        sensorSiteDetails.add(sensorSite);
    }

    public List<SensorSiteDetails> getSensorSites() {
        return sensorSiteDetails;
    }

    public void addSensor() {

    }
}
