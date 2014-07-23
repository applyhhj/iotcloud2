namespace java cgl.iotcloud.core.master.thrift

struct THeartBeatResponse {
    1:i32 totalSensors
    2:i32 responsiveSonsor
}

struct THeartBeatRequest {
    1:i32 sensors
}
