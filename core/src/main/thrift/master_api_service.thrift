namespace java cgl.iotcloud.core.api.thrift

include "sensor.thrift"
include "api.thrift"

service TMasterAPIService {
    list<string> getSites();

    api.TSiteDetailsResponse getSite(1:string siteId)

    api.TResponse deploySensor(1:list<string> sites, 2:api.TSensorDeployDescriptor sensor)
    api.TResponse unDeploySensor(1:list<string> sites, 2:api.TSensorId id)
    api.TResponse unDeployAllSensor(1:api.TSensorId id)
    api.TResponse startSensor(1:list<string> sites, 2:api.TSensorId id)
    api.TResponse startAllSensor(1:api.TSensorId id)
    api.TResponse stopSiteSensors(1:list<string> sites, 2:api.TSensorId id)
    api.TResponse stopAllSensors(1:api.TSensorId id)

    list<api.TSensor> getSensors(1:string siteId)
}