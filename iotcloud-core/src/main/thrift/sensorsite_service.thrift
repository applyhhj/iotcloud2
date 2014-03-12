namespace java cgl.iotcloud.core.sensorsite.thrift

include "sensor.thrift"

service TSensorSiteService {
    sensor.THeartBeatResponse hearbeat(1:sensor.THeartBeatRequest heartBeat)
}