package cgl.iotcloud.core.master.events;

import cgl.iotcloud.core.api.thrift.TSite;
import cgl.iotcloud.core.desc.SiteDescriptor;
import cgl.iotcloud.core.master.SiteState;

public class MSiteEvent {
    private SiteState state;

    private String siteId;

    private SiteDescriptor descriptor;

    private TSite site;

    public MSiteEvent(String siteId, SiteState state) {
        this.siteId = siteId;
        this.state = state;
    }

    public MSiteEvent(String siteId, SiteState state, TSite site) {
        this.siteId = siteId;
        this.state = state;
        this.site = site;
    }

    public MSiteEvent(String siteId, SiteState state, SiteDescriptor descriptor) {
        this.siteId = siteId;
        this.state = state;
        this.descriptor = descriptor;
    }

    public SiteState getState() {
        return state;
    }

    public String getSiteId() {
        return siteId;
    }

    public SiteDescriptor getDescriptor() {
        return descriptor;
    }

    public TSite getSite() {
        return site;
    }
}
