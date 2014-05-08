package cgl.iotcloud.transport.mqtt;

import org.fusesource.hawtbuf.Buffer;

public class MQTTMessage {
    private Buffer body;

    private String queue;

    public MQTTMessage(Buffer body, String queue) {
        this.body = body;
        this.queue = queue;
    }

    public Buffer getBody() {
        return body;
    }

    public String getQueue() {
        return queue;
    }
}
