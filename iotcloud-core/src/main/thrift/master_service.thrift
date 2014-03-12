namespace java cgl.iotcloud.core.master.thrift

struct TSensorId {
    1:string name
    2:string group
}

enum TDirection {
    IN,
    OUT
}

struct TChannel {
    1:string transport
    2:TDirection direction
}

struct TSensor {
    1:TSensorId id
    3:binary metadata
    4:list<TChannel> channels
}

enum ResponseState {
    SUCCESS,
    FAILURE
}

struct TRegisterSiteResponse {
    1:ResponseState state
    2:string statusMessage
}

struct TRegisterSiteRequest {
    1:required string siteId
    2:required i32 port
    3:required string host
}

struct TResponse {
    1:ResponseState state
    2:string statusMessage
}

service TMasterService {
    TRegisterSiteResponse registerSite(1:TRegisterSiteRequest request)

    TResponse registerSensor(1:string siteId, 2:TSensor sensor)
    TResponse unRegisterSensor(1:string siteId, 2:TSensorId sensor)
    TResponse updateSensor(1:string siteId, 2:TSensor sensor)
}





















