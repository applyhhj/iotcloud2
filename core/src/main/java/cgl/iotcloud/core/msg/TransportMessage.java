package cgl.iotcloud.core.msg;

import java.util.HashMap;
import java.util.Map;

public class TransportMessage {
    private Map<String, String> headers = new HashMap<String, String>();

    private byte[] body;

    public TransportMessage(byte[] body) {
        this.body = body;
    }

    public TransportMessage(Map<String, String> headers, byte[] body) {
        this.headers = headers;
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }
}
