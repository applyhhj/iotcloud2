package cgl.iotcloud.core.sensorsite;

import java.util.ArrayList;
import java.util.List;

public class SensorDeployDescriptor {
    private String jarName;

    private String className;

    private List<String> deploySites = new ArrayList<String>();

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

    @Override
    public String toString() {
        return "SensorDeployDescriptor{" +
                "jarName='" + jarName + '\'' +
                ", className='" + className + '\'' +
                '}';
    }
}
