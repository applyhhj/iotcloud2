package cgl.iotcloud.core.master;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.api.thrift.TResponse;
import cgl.iotcloud.core.api.thrift.TResponseState;
import cgl.iotcloud.core.api.thrift.TSensorDetails;
import cgl.iotcloud.core.api.thrift.TSensorId;
import cgl.iotcloud.core.master.thrift.THeartBeatRequest;
import cgl.iotcloud.core.sensorsite.SensorDeployDescriptor;
import cgl.iotcloud.core.sensorsite.thrift.TSensorSiteService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorSiteClient {
    private static Logger LOG = LoggerFactory.getLogger(SensorSiteClient.class);

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

    public boolean deploySensor(SensorDeployDescriptor deployDescriptor) {
        TSensorDetails sensorDetails = new TSensorDetails(deployDescriptor.getJarName(),
                deployDescriptor.getClassName());
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
}
