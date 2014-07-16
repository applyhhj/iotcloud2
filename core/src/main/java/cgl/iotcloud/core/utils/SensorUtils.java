package cgl.iotcloud.core.utils;

import cgl.iotcloud.core.api.thrift.TSensorState;
import cgl.iotcloud.core.sensorsite.SensorState;

/**
 * Contains various utility functions for sensors
 */
public class SensorUtils {
    public static TSensorState getSensorState(SensorState state) {
        if (state == SensorState.ACTIVATE) {
            return TSensorState.ACTIVE;
        } else if (state == SensorState.DEACTIVATE) {
            return TSensorState.DE_ACTIVATE;
        } else if (state == SensorState.DEPLOY) {
            return TSensorState.DEPLOY;
        } else if (state == SensorState.UN_DEPLOY) {
            return TSensorState.UN_DEPLOY;
        } else if (state == SensorState.UPDATE) {
            return TSensorState.UPDATE;
        }
        return TSensorState.UNKNOWN;
    }
}
