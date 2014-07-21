package cgl.iotcloud.core.desc;

public class SiteDescriptor {
    private String siteId;

    public SiteDescriptor() {
    }

    public SiteDescriptor(String siteId) {
        this.siteId = siteId;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
}
