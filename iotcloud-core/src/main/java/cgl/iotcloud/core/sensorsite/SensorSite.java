package cgl.iotcloud.core.sensorsite;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.Utils;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

public class SensorSite {
    private static Logger LOG = LoggerFactory.getLogger(SensorSite.class);

    private SiteContext siteContext;

    private SensorDeployer sensorDeployer;

    private THsHaServer server;

    private Map conf;

    private BlockingQueue<SensorEvent> sensorEvents;

    public void start() {
        // read the configuration file
        conf = Utils.readConfig();

        // create the site context
        siteContext = new SiteContext();

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

        // start the transports for sensor messages
        for (Transport t : siteContext.getTransports().values()) {
            t.start();
        }

        sensorEvents = new ArrayBlockingQueue<SensorEvent>(1024);

        sensorDeployer = new SensorDeployer(conf, siteContext, sensorEvents);
        sensorDeployer.start();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // now start the server to listen for the master commands
                try {
                    String host = Configuration.getSensorSiteHost(conf);
                    int port = Configuration.getSensorSitePort(conf);
                    InetSocketAddress addres = new InetSocketAddress(host, port);

                    TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(addres);
                    server = new THsHaServer(
                            new THsHaServer.Args(serverTransport).processor(
                                    new TSensorSiteService.Processor <SensorSiteService>(
                                            new SensorSiteService(siteContext, sensorEvents))).executorService(
                                    Executors.newFixedThreadPool(Configuration.getSensorSiteThreads(conf))));
                    server.serve();
                } catch (TTransportException e) {
                    String msg = "Error starting the Thrift server";
                    LOG.error(msg);
                    throw new RuntimeException(msg);
                }
            }
        });
        t.run();
    }

    public void stop() {
        // stop the deployer
        sensorDeployer.stop();

        // stop the transports
        for (Transport t : siteContext.getTransports().values()) {
            t.stop();
        }

        // stop the server
        server.stop();
    }

    public static void main(String[] args) {
        final SensorSite site = new SensorSite();
        site.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                site.stop();
            }
        });
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
