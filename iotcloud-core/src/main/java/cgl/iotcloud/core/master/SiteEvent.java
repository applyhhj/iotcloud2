package cgl.iotcloud.core.master;

public class SiteEvent {
    private State status;

    private String siteId;

    public SiteEvent(String siteId, State status) {
        this.status = status;
        this.siteId = siteId;
    }

    public State getStatus() {
        return status;
    }

    public String getSiteId() {
        return siteId;
    }

    public static enum State {
        ACTIVE,
        DEACTIVATED,
        ADDED
    }
}
