package cgl.iotcloud.core.master;

import cgl.iotcloud.core.master.events.MSiteEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterSiteController {
    private Logger LOG = LoggerFactory.getLogger(MasterSiteController.class);

    private MasterContext context;

    private HeartBeats heartBeats;

    public MasterSiteController(MasterContext context, EventBus siteEvents) {
        this.context = context;
        this.heartBeats = new HeartBeats(siteEvents);
    }

    @Subscribe
    public void handleEvent(MSiteEvent event) {
        if (event.getState() == SiteState.DEACTIVATED) {
            LOG.info("Deactivating the site {}", event.getSiteId());
            context.makeSiteOffline(event.getSiteId());
            // stop the timers
            heartBeats.stopForSite(event.getSiteId());
        } else if (event.getState() == SiteState.ACTIVE) {
            LOG.info("Activating the site {}", event.getSiteId());

            SiteDescriptor descriptor = context.getSensorSite(event.getSiteId());
            heartBeats.scheduleForSite(event.getSiteId(), descriptor.getHost(), descriptor.getPort());
        } else if (event.getState() == SiteState.ADDED) {
            SiteDescriptor descriptor = context.getSensorSite(event.getSiteId());
            LOG.info("A new site added {} with host {} and port {}", event.getSiteId(), descriptor.getHost(), descriptor.getPort());

            heartBeats.scheduleForSite(event.getSiteId(), descriptor.getHost(), descriptor.getPort());
        }
    }

    private void deactivateSite(String siteId) {

    }
}
