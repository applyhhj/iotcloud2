package cgl.iotcloud.core.sensorsite;

public class SensorDeployDescriptor {
    private String jarName;

    private String className;

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

    @Override
    public String toString() {
        return "SensorDeployDescriptor{" +
                "jarName='" + jarName + '\'' +
                ", className='" + className + '\'' +
                '}';
    }
}
