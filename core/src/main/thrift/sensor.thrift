namespace java cgl.iotcloud.core.master.thrift

struct TSite {
    1:required string siteid;
}

struct TRegisterSiteRequest {
    1:required string siteId
    2:required i32 port
    3:required string host
    4:optional binary metadata
}

struct THeartBeatResponse {
    1:i32 totalSensors
    2:i32 responsiveSonsor
}

struct THeartBeatRequest {
    1:i32 sensors
}
