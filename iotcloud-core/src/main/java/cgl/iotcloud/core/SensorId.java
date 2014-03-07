package cgl.iotcloud.core;

public class SensorId {
    private final String name;

    private final String group;

    public SensorId(String name, String group) {
        this.name = name;
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }
}
