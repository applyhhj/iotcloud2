namespace java cgl.iotcloud.core.sensorsite.thrift

include "sensor.thrift"
include "api.thrift"

service TSensorSiteService {
    sensor.THeartBeatResponse hearbeat(1:sensor.THeartBeatRequest heartBeat)

    api.TSensorDeployResponse deploySensor(1:api.TSensorDetails sensor)

    api.TResponse startSensor(1:list<string> sites, 2:api.TSensorId id)
    api.TResponse stopSensor(1:list<string> sites, 2:api.TSensorId id)
}