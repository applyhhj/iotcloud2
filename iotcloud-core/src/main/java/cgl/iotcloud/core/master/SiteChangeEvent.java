package cgl.iotcloud.core.master;

public class SiteChangeEvent {
    private SiteStatus status;

    private String id;

    public SiteChangeEvent(String id, SiteStatus status) {
        this.status = status;
        this.id = id;
    }

    public SiteStatus getStatus() {
        return status;
    }

    public String getId() {
        return id;
    }
}
