namespace java cgl.iotcloud.core.api.thrift

include "sensor.thrift"
include "api.thrift"

service TMasterAPIService {
    list<string> getSites();

    api.TSiteDetailsResponse getSite(1:string siteId)

    api.TResponse deploySensor(1:list<string> sites, 2:api.TSensorDetails sensor)
    api.TResponse startSensor(1:list<string> sites, 2:api.TSensorId id)
    api.TResponse stopSensor(1:list<string> sites, 2:api.TSensorId id)

    list<api.TSensorDetails> getSensors(1:string siteId)
}