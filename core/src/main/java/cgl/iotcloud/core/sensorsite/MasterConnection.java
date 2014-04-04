package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.Configuration;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MasterConnection {
    private static Logger LOG = LoggerFactory.getLogger(MasterConnection.class);

    private Map conf;

    private SiteContext siteContext;

    private long maxInterval = 1000;

    private long initialTime = 1;

    public MasterConnection(Map conf, SiteContext siteContext) {
        this.conf = conf;
        this.siteContext = siteContext;
    }

    public void start() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                long interval = initialTime;
                boolean success = false;

                while (!success) {
                    try {
                        if (!registerSite()) {
                            String msg = "Failed to register the site. stopping the site";
                            LOG.error(msg);
                            throw new RuntimeException(msg);
                        }
                        success = true;
                    } catch (TException e) {
                        if (interval < maxInterval && interval * 2 >= maxInterval) {
                            interval = maxInterval;
                            LOG.info("Reached the ceiling interval for trying to connect the master, trying every {} seconds", maxInterval);
                        } else if (interval == initialTime) {
                            LOG.info("Couldn't connect to master.. retrying");
                            interval *= 2;
                        } else {
                            interval *= 2;
                        }
                    }
                }
            }
        });
        t.start();
    }

    private boolean registerSite() throws TException {
        // now register the site
        String masterHost = Configuration.getMasterHost(conf);
        int masterServerPort = Configuration.getMasterServerPort(conf);
        MasterClient client = null;
        try {
            client = new MasterClient(masterHost, masterServerPort);

            String siteHost = Configuration.getSensorSiteHost(conf);
            int siteServerPort = Configuration.getSensorSitePort(conf);

            return client.registerSite(siteContext.getSiteId(), siteHost, siteServerPort);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
}
