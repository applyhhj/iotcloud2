package cgl.iotcloud.core.transport;

public class IdentityConverter implements MessageConverter {
    @Override
    public Object convert(Object input, Object context) {
        return input;
    }
}
