package cgl.sensorstream.core;

/**
 * Update for a data node
 */
public class Update {
    public enum Type {
        ADD, DELETE, UPDATE
    }

    private Type type;

    private String path;

    private byte[] data;

    public Update(Type type, String path) {
        this.type = type;
        this.path = path;
    }

    public Update(Type type, String path, byte[] data) {
        this.type = type;
        this.path = path;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public byte[] getData() {
        return data;
    }
}
