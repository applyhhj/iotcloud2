package cgl.iotcloud.core.master;

import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.api.thrift.*;
import cgl.iotcloud.core.master.thrift.TMasterService;
import cgl.iotcloud.core.master.thrift.TRegisterSiteRequest;
import cgl.iotcloud.core.transport.Direction;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterServiceHandler implements TMasterService.Iface {
    private static Logger LOG = LoggerFactory.getLogger(MasterServiceHandler.class);

    private MasterContext masterContext;

    public MasterServiceHandler(MasterContext masterContext) {
        this.masterContext = masterContext;
    }

    @Override
    public TResponse registerSite(TRegisterSiteRequest request) throws TException {
        String id = request.getSiteId();
        String host = request.getHost();
        int port = request.getPort();

        SensorSiteDescriptor descriptor = new SensorSiteDescriptor(id, port, host);
        descriptor.setMetadata(request.getMetadata());

        masterContext.addSensorSite(descriptor);

        TResponse registerSiteResponse = new TResponse();
        registerSiteResponse.setState(TResponseState.SUCCESS);
        return registerSiteResponse;
    }

    @Override
    public TResponse registerSensor(String siteId, TSensor sensor) throws TException {
        TSensorId id = sensor.getId();
        SensorId sensorID = new SensorId(id.getName(), id.getGroup());

        SensorDetails sensorDetails = new SensorDetails(sensorID);

        for (TChannel tChannel : sensor.getChannels()) {
            ChannelDetails details = null;
            if (tChannel.getDirection() == TDirection.IN) {
                details = new ChannelDetails(Direction.IN);
            } else if (tChannel.getDirection() == TDirection.OUT) {
                details = new ChannelDetails(Direction.OUT);
            }
            sensorDetails.addChannel(tChannel.getTransport(), details);
        }
        // TODO check
        sensorDetails.setMetadata(sensor.getMetadata());

        if (masterContext.addSensor(siteId, sensorDetails)) {
            return new TResponse(TResponseState.SUCCESS, "successfully added");
        } else {
            return new TResponse(TResponseState.FAILURE, "Failed to add the sensor");
        }
    }

    @Override
    public TResponse unRegisterSensor(String siteId, TSensorId id) throws TException {
        SensorId sensorID = new SensorId(id.getName(), id.getGroup());
        if (masterContext.removeSensor(siteId, sensorID)) {
            return new TResponse(TResponseState.SUCCESS, "successfully added");
        } else {
            return new TResponse(TResponseState.FAILURE, "Failed to remove the sensor");
        }
    }

    @Override
    public TResponse updateSensor(String siteId, TSensor sensor) throws TException {
        return null;
    }


}
