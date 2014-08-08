namespace java cgl.iotcloud.core.api.thrift

include "sensor.thrift"
include "api.thrift"

service TMasterAPIService {
    list<string> getSites();

    api.TSiteDetailsResponse getSite(1:string siteId)

    api.TResponse deploySensor(1:list<string> sites, 2:api.TSensorDeployDescriptor sensor)
    api.TResponse unDeploySensor(1:list<string> sites, 2:string  id)
    api.TResponse unDeployAllSensor(1:string id)
    api.TResponse startSensor(1:list<string> sites, 2:string  id)
    api.TResponse startAllSensor(1:string id)
    api.TResponse stopSiteSensors(1:list<string> sites, 2:string  id)
    api.TResponse stopAllSensors(1:string id)

    list<api.TSensor> getSensors(1:string siteId)
    list<api.TSensor> getAllSensors()
}