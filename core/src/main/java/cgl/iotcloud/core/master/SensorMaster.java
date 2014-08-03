package cgl.iotcloud.core.master;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.Utils;
import cgl.iotcloud.core.api.MasterAPIServiceHandler;
import cgl.iotcloud.core.api.thrift.TMasterAPIService;
import cgl.iotcloud.core.master.thrift.TMasterService;
import cgl.iotcloud.core.utils.SiteClientCache;
import cgl.iotcloud.core.zk.MasterPersistant;
import com.google.common.eventbus.EventBus;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;

public class SensorMaster {
    private static Logger LOG = LoggerFactory.getLogger(SensorMaster.class);

    // the api thrift server
    private THsHaServer apiServer;

    // the thrift server listening for sites
    private THsHaServer siteServer;

    // the configuration
    private Map conf;

    // master keeps all the moving parts here
    private MasterContext masterContext;

    // this event bus carries the events about the sensors
    private EventBus clientEventBus = new EventBus();

    // this event bus carries event about the sites
    private EventBus siteEventBus = new EventBus();

    // deploy sensors
    private ClientEventController clientEventController;

    // a factory
    private SiteClientCache siteClientCache;

    // this class manages the sensors and sites according to the events it receive
    private SiteEventController siteEventController;

    private MasterPersistant masterLoader;

    public void start() {
        // read the configuration file
        conf = Utils.readConfig();

        // create the context
        masterContext = new MasterContext(conf);

        // create the site client cache
        siteClientCache = new SiteClientCache(masterContext);

        // start the thread to manager the sites
        siteEventController = new SiteEventController(masterContext, siteEventBus);
        siteEventController.start();
        siteEventBus.register(siteEventController);

        // start the thread to manage the sensor deployments from clients
        clientEventController = new ClientEventController(siteClientCache, masterContext);
        clientEventController.start();
        // register this with sensor event bus
        clientEventBus.register(clientEventController);

        masterLoader = new MasterPersistant(masterContext);
        masterLoader.start();

        // now start the server to listen for the sites
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String host = Configuration.getMasterHost(conf);
                    int port = Configuration.getMasterServerPort(conf);
                    InetSocketAddress addres = new InetSocketAddress(host, port);

                    TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(addres);
                    siteServer = new THsHaServer(
                            new THsHaServer.Args(serverTransport).processor(
                                    new TMasterService.Processor <MasterServiceHandler>(
                                            new MasterServiceHandler(siteEventBus))).executorService(
                                    Executors.newFixedThreadPool(Configuration.getMasterServerThreads(conf))));
                    LOG.info("Starting the SensorMaster server on host: {} and port: {}", host, port);
                    siteServer.serve();
                } catch (TTransportException e) {
                    String msg = "Error starting the Thrift server";
                    LOG.error(msg, e);
                    throw new RuntimeException(msg, e);
                }
            }
        });
        t.start();

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                // start the server to listen for the API clients
                try {
                    String host = Configuration.getMasterHost(conf);
                    int port = Configuration.getMasterAPIPort(conf);
                    InetSocketAddress addres = new InetSocketAddress(host, port);

                    TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(addres);
                    apiServer = new THsHaServer(
                            new THsHaServer.Args(serverTransport).processor(
                                    new TMasterAPIService.Processor<MasterAPIServiceHandler>(
                                            new MasterAPIServiceHandler(masterContext, clientEventBus))).executorService(
                                    Executors.newFixedThreadPool(Configuration.getMasterAPIThreads(conf))));
                    LOG.info("Starting the SensorMaster API server on host: {} and port: {}", host, port);
                    apiServer.serve();
                } catch (TTransportException e) {
                    String msg = "Error starting the Thrift server";
                    LOG.error(msg, e);
                    throw new RuntimeException(msg, e);
                }
            }
        });
        t2.start();
    }

    public void stop() {
        // stop receiving requests from clients
        if (apiServer != null) {
            apiServer.stop();
        }
        // stop receiving requests from sites
        if (siteServer != null) {
            siteServer.stop();
        }

        // un register the even notifications
        clientEventBus.unregister(clientEventController);
    }

    public static void main(String[] args) {
        final SensorMaster master = new SensorMaster();

        master.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                master.stop();
            }
        });
    }
}
