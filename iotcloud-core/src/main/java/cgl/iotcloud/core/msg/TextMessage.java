package cgl.iotcloud.core.msg;

import java.io.Serializable;

public class TextMessage implements Serializable {
    private String text;

    public TextMessage(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
