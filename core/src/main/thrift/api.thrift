namespace java cgl.iotcloud.core.api.thrift

struct TSensorId {
    1:string name
    2:string group
}

enum TDirection {
    IN,
    OUT
}

enum TSensorState {
    DEPLOY,
    ACTIVE,
    DE_ACTIVATE,
    UN_DEPLOY,
    UPDATE
}

struct TChannel {
    1:string transport
    2:TDirection direction
}

struct TSensor {
    1:TSensorId id
    2:TSensorState state
    3:binary metadata
    4:list<TChannel> channels
}

enum TResponseState {
    SUCCESS,
    FAILURE
}

struct TResponse {
    1:TResponseState state
    2:string statusMessage
}

struct TSiteDetails {
    1:required string siteId
    2:required i32 port
    3:required string host
    4:optional binary metadata
}

struct TSiteDetailsResponse {
    1:optional TSiteDetails details
    2:required TResponse state
}

struct TSensorDetails {
    1:required string filename
    2:required string className
    3:optional map<string, string> properties
}

struct TSensorDeployResponse {
    1:optional list<string> deployedSites
    2:required TResponse response
}