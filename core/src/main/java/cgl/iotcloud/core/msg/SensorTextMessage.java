package cgl.iotcloud.core.msg;

import java.io.Serializable;

public class SensorTextMessage implements Serializable {
    private String text;

    public SensorTextMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
