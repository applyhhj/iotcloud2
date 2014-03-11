package cgl.iotcloud.core.master;

import cgl.iotcloud.core.sensorsite.thrift.THeartBeatRequest;
import cgl.iotcloud.core.sensorsite.thrift.TSensorSiteService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class SensorSiteClient {
    private TSensorSiteService.Client client;

    private int retries = 1;

    public SensorSiteClient(String host, int port) {
        TTransport transport = new TSocket(host, port);
        TProtocol protocol = new TBinaryProtocol(transport);

        this.client = new TSensorSiteService.Client(protocol);
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public boolean sendHearBeat() {
        boolean success = false;
        int errorCount = 0;

        while (!success && errorCount < retries) {
            THeartBeatRequest request = new THeartBeatRequest();
            request.setSensors(10);
            try {
                client.hearbeat(request);
                errorCount = 0;
                success = true;
            } catch (TException e) {
                success = false;
                errorCount++;
            }
        }
        return success;
    }
}
