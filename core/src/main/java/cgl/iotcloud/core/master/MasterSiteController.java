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
            LOG.info("Deactivating the site {}", event.getSiteId());
            context.makeSiteOffline(event.getSiteId());
            // stop the timers
            heartBeats.stopForSite(event.getSiteId());
        } else if (event.getState() == SiteState.ACTIVE) {
            LOG.info("Activating the site {}", event.getSiteId());

            SiteDescriptor descriptor = context.getSensorSite(event.getSiteId());
            heartBeats.scheduleForSite(event.getSiteId(), descriptor.getHost(), descriptor.getPort());
        } else if (event.getState() == SiteState.ADDED) {
            addSite(event);
        }
    }

    private void addSite(MSiteEvent event) {
        SiteDescriptor descriptor = event.getDescriptor();
        context.addSensorSite(descriptor);

        SensorUpdater.registerSite(curatorFramework, Configuration.getZkRoot(context.getConf()), descriptor);
        heartBeats.scheduleForSite(event.getSiteId(), descriptor.getHost(), descriptor.getPort());
        LOG.info("A new site added {} with host {} and port {}", event.getSiteId(), descriptor.getHost(), descriptor.getPort());
    }

    /**
     * Add the site to context
     * Add the site to zookeeper
     * @param siteId the site id
     */
    private void deactivateSite(String siteId) {

    }

    private void activateSite(String siteId) {

    }
}
