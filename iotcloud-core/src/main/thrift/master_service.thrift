namespace java cgl.iotcloud.thrift

struct TSensorId {
    1:string name
    2:string group
}

struct TChannel {
    1:string name
    2:string type
    3:string properties
}

struct TSensor {
    1:string name
    2:string group
    3:string properties
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
    1:string siteId
    2:int32 port
}

service TMasterService {
    TRegisterSiteResponse registerSite(1:TRegisterSiteRequest)

    TResponse registerSensor(1:TSensor sensor)
    TResponse unRegisterSensor(1:TSensorId sensor)
    TResponse updateSensor(1:TSensor sensor)
    TChannelResponse registerChannel(1:TSensorId sensorId, 2:TChannel channel)
    TChannelResponse unRegisterChannel(1:TSensorId sensorId, 2:TChannel channel)
    list<TNodeId> getNodes()
    TSensor getSensor(1:TNodeId id)
}





















