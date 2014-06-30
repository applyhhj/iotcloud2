package cgl.iotcloud.core.transport;

import cgl.iotcloud.core.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractTransport implements Transport {
    private static Logger LOG = LoggerFactory.getLogger(AbstractTransport.class);

    /**
     * Id of the site
     */
    protected String siteId;

    /**
     * This gateway is connected to these brokers
     */
    protected List<BrokerHost> brokerHosts;

    /**
     * Every transport has a list of applications. A group has specific channels registers to
     * it by the sensors
     */
    protected Map<String, Group> groups = new ConcurrentHashMap<String, Group>();


    @Override
    public void configure(String siteId, Map properties) {
        this.siteId = siteId;
        Map params = (Map)properties.get(Configuration.TRANSPORT_PROPERTIES);
        Object urlProp = params.get(TransportConstants.PROP_URLS);
        if (urlProp == null || !(urlProp instanceof List)) {
            String message = "Url is required by the Transport";
            LOG.error(message);
            throw new RuntimeException(message);
        }

        for (Object o : (List)urlProp) {
            if (o instanceof String) {
                String url = (String) o;
                String tokens[] = url.split(":");

                if (tokens.length == 2) {
                    brokerHosts.add(new BrokerHost(tokens[0], Integer.parseInt(tokens[1])));
                } else {
                    throw new RuntimeException("Each broker URL should be of the format host:port");
                }
            }
        }
    }

    @Override
    public void registerChannel(ChannelName name, Channel channel) {
        // check to see if we already have a group for this channel
        Group group = groups.get(channel.getGroup());
        if (group == null) {
            group = new Group(channel.getName());
            groups.put(channel.getGroup(), group);
        }


    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
