package cgl.iotcloud.core.sensorsite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorDeployDescriptor implements Serializable {
    private String jarName;

    private String className;

    private List<String> deploySites = new ArrayList<String>();

    private Map<String, String> properties = new HashMap<String, String>();

    public SensorDeployDescriptor(String jarName, String className) {
        this.jarName = jarName;
        this.className = className;
    }

    public String getJarName() {
        return jarName;
    }

    public String getClassName() {
        return className;
    }

    public void addDeploySites(List<String> sites) {
        deploySites.addAll(sites);
    }

    public List<String> getDeploySites() {
        return deploySites;
    }

    public void addProperty(String name, String value) {
        properties.put(name, value);
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "SensorDeployDescriptor{" +
                "jarName='" + jarName + '\'' +
                ", className='" + className + '\'' +
                '}';
    }
}
