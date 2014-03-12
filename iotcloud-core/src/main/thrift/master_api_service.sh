namespace java cgl.iotcloud.core.master.thrift

include sensor.thrift

struct TSiteDetails {
    1:required string siteId
    2:required i32 port
    3:required string host
    4:optional binary metadata
}

struct TSiteDetailsRequest {
    1:required string siteId
}

struct TSiteDetailsResponse {
    1:optional TSiteDetails details
    2:required ResponseState state
}

struct TSensorDetails {
    1:required string filename
    2:required string className
}

struct TDeploySensorRequest {
    1:required list<string> sites
    2:required TSensorDetails sensor
}

struct TSensorDeployResponse {
    1:optional list<string> deployedSites
    2:required ResponseState state
    4:optional string reason
}

service TMasterAPIService {
    TSiteDetailsResponse getSite(1:string siteId)

    TSensorDeployResponse deploySensor(1:list<string> sites, 2:TSensorDetails sensor)
    TResponse startSensor(1:list<string> sites, 2:TSensorId id)
    TResponse stopSensor(1:list<string> sites, 2:TSensorId id)
}