namespace java cgl.iotcloud.core.sensorsite.thrift

include "sensor.thrift"
include "api.thrift"

service TSensorSiteService {
    sensor.THeartBeatResponse hearbeat(1:sensor.THeartBeatRequest heartBeat)

    api.TResponse deploySensor(1:api.TSensorDeployDescriptor sensor)
    api.TResponse unDeploySensor(1:string id)

    api.TResponse startSensor(1:string id)
    api.TResponse stopSensor(1:string id)
}