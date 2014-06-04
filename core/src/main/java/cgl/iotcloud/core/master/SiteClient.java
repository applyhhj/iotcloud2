package cgl.iotcloud.core.master;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.api.thrift.TResponse;
import cgl.iotcloud.core.api.thrift.TResponseState;
import cgl.iotcloud.core.api.thrift.TSensorDetails;
import cgl.iotcloud.core.api.thrift.TSensorId;
import cgl.iotcloud.core.master.thrift.THeartBeatRequest;
import cgl.iotcloud.core.master.thrift.THeartBeatResponse;
import cgl.iotcloud.core.master.thrift.TMasterService;
import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;
import cgl.iotcloud.core.sensorsite.thrift.TSensorSiteService;
import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SiteClient {
    private static Logger LOG = LoggerFactory.getLogger(SiteClient.class);

    private TSensorSiteService.Client client;

    private int retries = 1;

    TTransport transport;

    public SiteClient(String host, int port) throws TTransportException {
        TSocket socket = new TSocket(host, port);
        transport = new TFramedTransport(socket);
        TProtocol protocol = new TBinaryProtocol(transport);
        this.client = new TSensorSiteService.Client(protocol);

        transport.open();
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public boolean sendHearBeat() throws TException {
        THeartBeatRequest request = new THeartBeatRequest();
        request.setSensors(10);
        THeartBeatResponse response = client.hearbeat(request);
        return response != null;
    }

    public boolean deploySensor(SensorDeployDescriptor deployDescriptor) {
        TSensorDetails sensorDetails = new TSensorDetails(deployDescriptor.getJarName(),
                deployDescriptor.getClassName());

        for (Map.Entry<String, String> e : deployDescriptor.getProperties().entrySet()) {
            sensorDetails.putToProperties(e.getKey(), e.getValue());
        }

        try {
            TResponse response = this.client.deploySensor(sensorDetails);

            if (response.getState() == TResponseState.SUCCESS) {
                return true;
            } else if (response.getState() == TResponseState.FAILURE) {
                LOG.error("Failed to deploy the sensor: {}", deployDescriptor);
                return false;
            }
            return false;
        } catch (TException e) {
            String msg = "Failed to deploy the sensor";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public boolean startSensor(SensorId id) {
        TSensorId sensorId = new TSensorId(id.getName(), id.getGroup());
        try {
            TResponse response = this.client.startSensor(sensorId);

            if (response.getState() == TResponseState.SUCCESS) {
                return true;
            } else if (response.getState() == TResponseState.FAILURE) {
                LOG.error("Failed to start the sensor: {}", id);
                return false;
            }
            return false;
        } catch (TException e) {
            String msg = "Failed to start the sensor";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public boolean stopSensor(SensorId id) {
        TSensorId sensorId = new TSensorId(id.getName(), id.getGroup());
        try {
            TResponse response = this.client.stopSensor(sensorId);

            if (response.getState() == TResponseState.SUCCESS) {
                return true;
            } else if (response.getState() == TResponseState.FAILURE) {
                LOG.error("Failed to stop the sensor: {}", id);
                return false;
            }
            return false;
        } catch (TException e) {
            String msg = "Failed to deploy the sensor";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public boolean unDeploySensor(SensorId id) {
        TSensorId sensorId = new TSensorId(id.getName(), id.getGroup());
        try {
            TResponse response = this.client.unDeploySensor(sensorId);

            if (response.getState() == TResponseState.SUCCESS) {
                return true;
            } else if (response.getState() == TResponseState.FAILURE) {
                LOG.error("Failed to stop the sensor: {}", id);
                return false;
            }
            return false;
        } catch (TException e) {
            String msg = "Failed to deploy the sensor";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public void close() {
        if (transport != null) {
            transport.close();
        }
    }
}
