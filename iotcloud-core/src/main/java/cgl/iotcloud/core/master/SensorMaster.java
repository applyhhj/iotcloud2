package cgl.iotcloud.core.master;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.Utils;
import cgl.iotcloud.core.api.MasterAPIServiceHandler;
import cgl.iotcloud.core.api.thrift.TMasterAPIService;
import cgl.iotcloud.core.master.store.InMemorySensorData;
import cgl.iotcloud.core.master.thrift.TMasterService;
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

    public static void main(String[] args) {
        // read the configuration file
        Map conf = Utils.readConfig();

        MasterContext masterContext = new MasterContext();

        // configures the sensor store
        InMemorySensorData sensorStore = new InMemorySensorData();

        // start the thread to manager the sites
        SiteController manager = new SiteController(masterContext);
        manager.start();

        // now start the server to listen for the sites
        try {
            String host = Configuration.getMasterHost(conf);
            int port = Configuration.getMasterServerPort(conf);
            InetSocketAddress addres = new InetSocketAddress(host, port);

            TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(addres);
            THsHaServer server = new THsHaServer(
                    new THsHaServer.Args(serverTransport).processor(
                            new TMasterService.Processor <MasterServiceHandler>(new MasterServiceHandler(masterContext))).executorService(
                            Executors.newFixedThreadPool(Configuration.getMasterServerThreads(conf))));
            server.serve();
            LOG.info("Started the SensorMaster server on host: {} and port: {}", host, port);
        } catch (TTransportException e) {
            String msg = "Error starting the Thrift server";
            LOG.error(msg);
            throw new RuntimeException(msg);
        }


        // now start the server to listen for the clients
        try {
            String host = Configuration.getMasterHost(conf);
            int port = Configuration.getMasterAPIPort(conf);
            InetSocketAddress addres = new InetSocketAddress(host, port);

            TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(addres);
            THsHaServer server = new THsHaServer(
                    new THsHaServer.Args(serverTransport).processor(
                            new TMasterAPIService.Processor <MasterAPIServiceHandler>(new MasterAPIServiceHandler(masterContext))).executorService(
                            Executors.newFixedThreadPool(Configuration.getMasterAPIThreads(conf))));
            server.serve();
            LOG.info("Started the SensorMaster server on host: {} and port: {}", host, port);
        } catch (TTransportException e) {
            String msg = "Error starting the Thrift server";
            LOG.error(msg);
            throw new RuntimeException(msg);
        }


    }
}
