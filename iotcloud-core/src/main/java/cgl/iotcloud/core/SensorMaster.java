package cgl.iotcloud.core;

import cgl.iotcloud.core.store.InMemorySensorData;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.Map;
import java.util.concurrent.Executors;

public class SensorMaster {
    public static void main(String[] args) {
        // read the configuration file
        Map conf = Utils.readConfig();

        // configures the sensor store
        InMemorySensorData sensorStore = new InMemorySensorData();

        // now start the server to listen for the sites
        try {
            TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(port);
            server = new THsHaServer(
                    new THsHaServer.Args(serverTransport).processor(
                            new TNodeService.Processor <NodeServiceHandler>(new NodeServiceHandler(ioTCloud))).executorService(Executors.newFixedThreadPool(10)));
            server.serve();
        } catch (TTransportException e) {
            String msg = "Error starting the Thrift server";
            log.error(msg);
            throw new IOTException(msg);
        }
    }
}
