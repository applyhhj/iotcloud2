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
    4:optional binary metadata
}

struct TResponse {
    1:ResponseState state
    2:string statusMessage
}

struct THeartBeatResponse {
    1:i32 totalSensors
    2:i32 responsiveSonsor
}

struct THeartBeatRequest {
    1:i32 sensors
}