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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SensorId sensorId = (SensorId) o;

        if (!group.equals(sensorId.group)) return false;
        if (!name.equals(sensorId.name)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + group.hashCode();
        return result;
    }
}
