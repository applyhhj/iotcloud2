namespace java cgl.iotcloud.core.sensorsite.thrift

include sensor.thrift

service TSensorSiteService {
    THeartBeatResponse hearbeat(1:THeartBeatRequest heartBeat)
}