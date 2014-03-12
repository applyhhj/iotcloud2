package cgl.iotcloud.core.master;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.Utils;
import cgl.iotcloud.core.master.store.InMemorySensorData;
import cgl.iotcloud.core.master.thrift.TMasterService;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        SiteMonitor monitor = new SiteMonitor(masterContext);
        monitor.start();

        // now start the server to listen for the sites
        int port = Configuration.getMasterServerPort(conf);
        try {
            TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(port);
            THsHaServer server = new THsHaServer(
                    new THsHaServer.Args(serverTransport).processor(
                            new TMasterService.Processor <MasterServiceHandler>(new MasterServiceHandler(masterContext))).executorService(Executors.newFixedThreadPool(10)));
            server.serve();
        } catch (TTransportException e) {
            String msg = "Error starting the Thrift server";
            LOG.error(msg);
            throw new RuntimeException(msg);
        }

        // we wait sometime for the sensor sites to come online
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // start the thread to monitor the sites
        // for each of the sites registered we need to start a separate task
    }
}
