package cgl.iotcloud.core.utils;

import cgl.iotcloud.core.master.MasterContext;
import cgl.iotcloud.core.master.SiteClient;
import cgl.iotcloud.core.desc.SiteDescriptor;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Acts as a factory for getting the clients that are required to talk to the sensor sites
 */
public class SiteClientCache {
    private static Logger LOG = LoggerFactory.getLogger(SiteClientCache.class);

    private Map<String, SiteClient> siteClients = new HashMap<String, SiteClient>();

    private MasterContext context;

    public SiteClientCache(MasterContext context) {
        this.context = context;
    }

    public SiteClient getSiteClient(String siteId) throws Exception {
        if (siteClients.containsKey(siteId)) {
            return siteClients.get(siteId);
        } else {
            SiteDescriptor descriptor = context.getSensorSite(siteId);
            if (descriptor != null) {
                try {
                    SiteClient client = new SiteClient(descriptor.getHost(), descriptor.getPort());

                    siteClients.put(siteId, client);
                    return client;
                } catch (TTransportException e) {
                    LOG.warn("Failed to obtain a client for site {}", siteId);
                    throw new Exception("Failed to obtain client for site " + siteId);
                }
            }
        }
        return null;
    }

    public void markClientFailed(String siteId) {
        SiteClient client = siteClients.get(siteId);
        if (client != null) {
            client.close();
        }
        siteClients.remove(siteId);
    }
}
