struct THeartBeatResponse {
    1:int32 totalSensors
    2:int32 responsiveSonsor
}

struct THeartBeatRequest {
    1:int32 sensors
}

service TSensorSiteService {
    THeartBeatResponse hearbeat(1:THeartBeatRequest heartBeat)
}