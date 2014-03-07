namespace java cgl.iotcloud.thrift

struct TProperty {
    1:string name
    2:string value
}

struct TEndpointRequest {
    1:string name
    2:string type
    3:string path
}

struct TEndpointResponse {
    1:string name
    2:string type
    3:string address
    4:list<TProperty> properties
}

struct TSensorId {
    1:string name
    2:string group
}

struct TResponse {
    1:i32 status
    2:string reason
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

service TMasterService {
    TResponse registerSite(1:

    TResponse registerSensor(1:TSensor sensor)
    TResponse unRegisterSensor(1:TSensor sensor)
    TChannelResponse registerChannel(1:TSensorId sensorId, 2:TChannel channel)
    TChannelResponse unRegisterChannel(1:TSensorId sensorId, 2:TChannel channel)
    list<TNodeId> getNodes()
    TSensor getSensor(1:TNodeId id)
}





















