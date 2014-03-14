package cgl.iotcloud.core.master;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.Utils;
import cgl.iotcloud.core.api.MasterAPIServiceHandler;
import cgl.iotcloud.core.api.thrift.TMasterAPIService;
import cgl.iotcloud.core.master.thrift.TMasterService;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

public class SensorMaster {
    private static Logger LOG = LoggerFactory.getLogger(SensorMaster.class);

    // this class manages the sensors and sites according to the events it receive
    private SiteController manager;

    // the api thrift server
    private THsHaServer apiServer;

    // the thrift server listening for sites
    private THsHaServer siteServer;

    private Map conf;

    private MasterContext masterContext;

    private BlockingQueue<SiteEvent> siteEventsQueue;

    private BlockingQueue<MasterSensorEvent> sensorEvents;

    public void start() {
        // read the configuration file
        conf = Utils.readConfig();

        // create the context
        masterContext = new MasterContext();

        // the queues for handing the incoming requests
        siteEventsQueue = new ArrayBlockingQueue<SiteEvent>(1024);
        sensorEvents = new ArrayBlockingQueue<MasterSensorEvent>(1024);

        // start the thread to manager the sites
        manager = new SiteController(masterContext, siteEventsQueue, sensorEvents);
        manager.start();

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
                                    new TMasterService.Processor <MasterServiceHandler>(new MasterServiceHandler(masterContext, siteEventsQueue, sensorEvents))).executorService(
                                    Executors.newFixedThreadPool(Configuration.getMasterServerThreads(conf))));
                    LOG.info("Starting the SensorMaster server on host: {} and port: {}", host, port);
                    siteServer.serve();
                } catch (TTransportException e) {
                    String msg = "Error starting the Thrift server";
                    LOG.error(msg);
                    throw new RuntimeException(msg);
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
                                            new MasterAPIServiceHandler(masterContext, sensorEvents))).executorService(
                                    Executors.newFixedThreadPool(Configuration.getMasterAPIThreads(conf))));
                    LOG.info("Starting the SensorMaster server on host: {} and port: {}", host, port);
                    apiServer.serve();
                } catch (TTransportException e) {
                    String msg = "Error starting the Thrift server";
                    LOG.error(msg);
                    throw new RuntimeException(msg);
                }
            }
        });
        t2.start();

    }

    public void stop() {
        // stop receiving requests from clients
        apiServer.stop();
        // stop handling the controller requests
        manager.stop();
        // stop receiving requests from sites
        siteServer.stop();
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
