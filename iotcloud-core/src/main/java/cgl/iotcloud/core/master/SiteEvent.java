package cgl.iotcloud.core.master;

public class SiteEvent {
    private State status;

    private String id;

    public SiteEvent(String id, State status) {
        this.status = status;
        this.id = id;
    }

    public State getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }

    public static enum State {
        ACTIVE,
        DEACTIVATED
    }
}
