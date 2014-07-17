namespace java cgl.iotcloud.core.master.thrift

include "sensor.thrift"
include "api.thrift"

service TMasterService {
    api.TResponse registerSite(1:sensor.TRegisterSiteRequest request)
    api.TResponse unRegisterSite(1:sensor.TSite site)

    api.TResponse registerSensor(1:string siteId, 2:api.TSensor sensor)
    api.TResponse unRegisterSensor(1:string siteId, 2:api.TSensorId sensor)
    api.TResponse updateSensor(1:string siteId, 2:api.TSensor sensor)
}





















