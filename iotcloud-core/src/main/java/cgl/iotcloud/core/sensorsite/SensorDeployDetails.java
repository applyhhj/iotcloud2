package cgl.iotcloud.core.sensorsite;

public class SensorDeployDetails {
    private String jarName;

    private String className;

    public SensorDeployDetails(String jarName, String className) {
        this.jarName = jarName;
        this.className = className;
    }

    public String getJarName() {
        return jarName;
    }

    public String getClassName() {
        return className;
    }
}
