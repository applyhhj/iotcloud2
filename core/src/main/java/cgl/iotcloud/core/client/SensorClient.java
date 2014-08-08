package cgl.iotcloud.core.client;

import cgl.iotcloud.core.Configuration;
import cgl.iotcloud.core.api.thrift.*;
import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.List;
import java.util.Map;

public class SensorClient {
    private TMasterAPIService.Client client;

    private TTransport transport;

    public static SensorClient getConfiguredClient(Map conf) {
        try {
            return new SensorClient(conf);
        } catch (TTransportException e) {
            throw new RuntimeException(e);
        }
    }

    public SensorClient(Map conf) throws TTransportException {
        String host = Configuration.getMasterHost(conf);
        int port = Configuration.getMasterAPIPort(conf);

        TSocket socket = new TSocket(host, port);
        this.transport = new TFramedTransport(socket);

        TProtocol protocol = new TBinaryProtocol(transport);
        transport.open();
        this.client = new TMasterAPIService.Client(protocol);
    }

    public List<String> getSensorSites() {
        try {
            return this.client.getSites();
        } catch (TException e) {
            throw new RuntimeException("Error occurred while getting the registered sites", e);
        }
    }

    public TSite getSensorSite(String id) {
        try {
            TSiteDetailsResponse response = this.client.getSite(id);
            if (response.getState().getState() == TResponseState.SUCCESS) {
                return response.getDetails();
            }
            return null;
        } catch (TException e) {
            throw new RuntimeException("Error occurred while getting the registered sites", e);
        }
    }

    public List<TSensor> getSensors() {
        try {
            return this.client.getAllSensors();
        } catch (TException e) {
            throw new RuntimeException("Error occurred while getting the registered sensors", e);
        }
    }

    public List<TSensor> getSensors(String site) {
        try {
            return this.client.getSensors(site);
        } catch (TException e) {
            throw new RuntimeException("Error occurred while getting the registered sensors", e);
        }
    }

    public boolean deploySensor(SensorDeployDescriptor deployDescriptor) {
        TSensorDeployDescriptor sensorDetails = new TSensorDeployDescriptor(deployDescriptor.getJarName(), deployDescriptor.getClassName());

        for (Map.Entry<String, String> e : deployDescriptor.getProperties().entrySet()) {
            sensorDetails.putToProperties(e.getKey(), e.getValue());
        }

        try {
            TResponse response = client.deploySensor(deployDescriptor.getDeploySites(), sensorDetails);
            return response.getState() == TResponseState.SUCCESS;
        } catch (TException e) {
            throw new RuntimeException("Failed to deploy the sensor", e);
        }
    }

    public boolean stopSensor(String id) {
        try {
            client.stopAllSensors(id);
        } catch (TException e) {
            throw new RuntimeException("Failed to stop the sensors", e);
        }
        return true;
    }

    public boolean unDeploySensor(String id) {
        try {
            client.unDeployAllSensor(id);
        } catch (TException e) {
            throw new RuntimeException("Failed to unDeploy the sensors", e);
        }
        return true;
    }

    public boolean startSensor(String id) {
        try {
            client.stopAllSensors(id);
        } catch (TException e) {
            throw new RuntimeException("Failed to unDeploy the sensors", e);
        }
        return true;
    }

    public void close() {
        transport.close();
    }
}
