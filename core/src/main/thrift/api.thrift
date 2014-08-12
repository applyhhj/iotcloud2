namespace java cgl.iotcloud.core.api.thrift

enum TDirection {
    IN,
    OUT
}

enum TSensorState {
    DEPLOY,
    ACTIVE,
    DE_ACTIVATE,
    UN_DEPLOY,
    UPDATE,
    UNKNOWN
}

struct TChannel {
    1:string transport
    2:TDirection direction
    3:optional map<string,string> properties
    4:optional string brokerUrl
    5:optional string name
    6:optional string site
    7:optional string sensor
    8:optional bool grouped
}

struct TBroker {
    1:string name
    2:map<string, string> properties
}

struct TSensor {
    1:string name
    2:string siteId
    3:TSensorState state
    4:binary metadata
    5:list<TChannel> channels
    6:optional string sensorId       # a unique id for the sensor
}

enum TResponseState {
    SUCCESS,
    FAILURE
}

struct TResponse {
    1:TResponseState state
    2:string statusMessage
}

struct TSite {
    1:required string siteId
    2:required i32 port
    3:required string host
    4:optional binary metadata
    5:optional list<TBroker> brokers
}

struct TSiteDetailsResponse {
    1:optional TSite details
    2:required TResponse state
}

struct TSensorDeployDescriptor {
    1:required string filename
    2:required string className
    3:optional map<string, string> properties
}

struct TSensorDeployResponse {
    1:optional list<string> deployedSites
    2:required TResponse response
}
