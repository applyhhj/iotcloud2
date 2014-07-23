package cgl.iotcloud.core.master;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.desc.SiteDescriptor;
import cgl.iotcloud.core.master.events.MSiteEvent;
import cgl.iotcloud.core.zk.SensorUpdater;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterSiteController {
    private Logger LOG = LoggerFactory.getLogger(MasterSiteController.class);

    private MasterContext context;

    private HeartBeats heartBeats;

    private CuratorFramework curatorFramework;

    public MasterSiteController(MasterContext context, EventBus siteEvents) {
        this.context = context;
        this.heartBeats = new HeartBeats(siteEvents);

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.curatorFramework = CuratorFrameworkFactory.newClient(Configuration.getZkConnectionString(context.getConf()), retryPolicy);
    }

    public void start() {
        this.curatorFramework.start();
    }

    @Subscribe
    public void handleEvent(MSiteEvent event) {
        if (event.getState() == SiteState.DEACTIVATED) {
           deactivateSite(event);
        } else if (event.getState() == SiteState.ACTIVE) {
            activateSite(event);
        } else if (event.getState() == SiteState.ADDED) {
            addSite(event);
        }
    }

    private void addSite(MSiteEvent event) {
        SiteDescriptor descriptor = event.getDescriptor();
        context.addSensorSite(descriptor);

        SensorUpdater.addSite(curatorFramework, Configuration.getZkRoot(context.getConf()), descriptor);
        heartBeats.scheduleForSite(event.getSiteId(), descriptor.getHost(), descriptor.getPort());
        LOG.info("A new site added {} with host {} and port {}", event.getSiteId(), descriptor.getHost(), descriptor.getPort());
    }

    private void deactivateSite(MSiteEvent event) {
        context.makeSiteOffline(event.getSiteId());
        // stop the timers
        heartBeats.stopForSite(event.getSiteId());
        LOG.info("Deactivating the site {}", event.getSiteId());
    }

    private void activateSite(MSiteEvent event) {
        SiteDescriptor descriptor = context.getSensorSite(event.getSiteId());
        heartBeats.scheduleForSite(event.getSiteId(), descriptor.getHost(), descriptor.getPort());

        LOG.info("Activating the site {}", event.getSiteId());
    }
}
