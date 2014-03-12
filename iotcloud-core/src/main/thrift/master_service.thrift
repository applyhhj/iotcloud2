namespace java cgl.iotcloud.core.master.thrift

include sensor.thrift

service TMasterService {
    TRegisterSiteResponse registerSite(1:TRegisterSiteRequest request)

    TResponse registerSensor(1:string siteId, 2:TSensor sensor)
    TResponse unRegisterSensor(1:string siteId, 2:TSensorId sensor)
    TResponse updateSensor(1:string siteId, 2:TSensor sensor)
}





















