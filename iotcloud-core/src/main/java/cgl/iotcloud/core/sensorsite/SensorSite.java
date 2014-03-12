package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.Utils;
import cgl.iotcloud.core.master.MasterServiceHandler;
import cgl.iotcloud.core.master.store.InMemorySensorData;
import cgl.iotcloud.core.master.thrift.TMasterService;
import cgl.iotcloud.core.sensorsite.thrift.TSensorSiteService;
import cgl.iotcloud.core.transport.Transport;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;

public class SensorSite {
    private static Logger LOG = LoggerFactory.getLogger(SensorSite.class);

    public static void main(String[] args) {
        // read the configuration file
        Map conf = Utils.readConfig();

        // create the site context
        SiteContext siteContext = new SiteContext();

        // configures the sensor store
        InMemorySensorData sensorStore = new InMemorySensorData();

        // read the available transports and register them
        Map transports = Configuration.getTransports(conf);
        if (transports == null) {
            String msg = "At least one transport must be configured";
            LOG.error(msg);
            throw new RuntimeException(msg);
        }

        // load the transport files and add them to context
        for (Object e : transports.entrySet()) {
            if (e instanceof Map.Entry) {
                Object tName = ((Map.Entry) e).getKey();
                Object tConf = ((Map.Entry) e).getValue();
                if (!(tName instanceof String)) {
                    String msg = "The transport name should be an string";
                    LOG.error(msg);
                    throw new RuntimeException(msg);
                }
                if (!(tConf instanceof Map)) {
                    String msg = "The transport configurations should be in a map";
                    LOG.error(msg);
                    throw new RuntimeException(msg);
                }

                Transport t = loadTransport((Map) tConf);
                // configure the transport, this doesn't start the transport
                t.configure((Map) tConf);
                siteContext.addTransport((String) tName, t);
            }
        }

        // now start the server to listen for the master commands
        try {
            String host = Configuration.getSensorSiteHost(conf);
            int port = Configuration.getSensorSitePort(conf);
            InetSocketAddress addres = new InetSocketAddress(host, port);

            TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(addres);
            THsHaServer server = new THsHaServer(
                    new THsHaServer.Args(serverTransport).processor(
                            new TSensorSiteService.Processor <SensorSiteService>(
                                    new SensorSiteService(siteContext))).executorService(
                            Executors.newFixedThreadPool(Configuration.getSensorSiteThreads(conf))));
            server.serve();
        } catch (TTransportException e) {
            String msg = "Error starting the Thrift server";
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
    }

    private static Transport loadTransport(Map transportConf) {
        String className = Configuration.getTransportClass(transportConf);

        try {
            Class<?> clazz = Class.forName(className);

            Class<? extends Transport> runClass = clazz.asSubclass(Transport.class);
            // Avoid Class.newInstance, for it is evil.
            Constructor<? extends Transport> ctor = runClass.getConstructor();

            return ctor.newInstance();
        } catch (ClassNotFoundException x) {
            LOG.error("Transport class cannot be found {}", className, x);
            throw new RuntimeException("Transport class cannot be found " + className, x);
        } catch (InstantiationException x) {
            LOG.error("Transport class cannot be instantiated {}", className, x);
            throw new RuntimeException("Transport class cannot be instantiated " + className, x);
        } catch (Exception e) {
            LOG.error("Error loading the class {}", className, e);
            throw new RuntimeException("Error loading the class " + className, e);
        }
    }
}
