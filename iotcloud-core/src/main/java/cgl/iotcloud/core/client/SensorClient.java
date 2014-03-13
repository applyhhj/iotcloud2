package cgl.iotcloud.core.client;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.api.thrift.TMasterAPIService;
import cgl.iotcloud.core.sensorsite.thrift.TSensorSiteService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.util.Map;

public class SensorClient {
    private TMasterAPIService.Client client;

    private Map conf;

    public SensorClient(Map conf) {
        this.conf = conf;

        String host = Configuration.getMasterHost(conf);
        int port = Configuration.getMasterAPIPort(conf);

        TTransport transport = new TSocket(host, port);
        TProtocol protocol = new TBinaryProtocol(transport);

        this.client = new TMasterAPIService.Client(protocol);
    }


}
