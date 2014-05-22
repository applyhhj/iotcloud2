package cgl.iotcloud.core.msg;

public class SensorTextMessage extends AbstractMessage {
    private String text;

    public SensorTextMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }
}
