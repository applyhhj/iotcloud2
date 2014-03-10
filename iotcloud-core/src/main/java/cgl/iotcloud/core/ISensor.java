package cgl.iotcloud.core;

import java.util.Map;

public interface ISensor {
    Configurator getConfigurator(Map conf);

    /**
     * Called when a task for this component is initialized within a worker on the cluster.
     * It provides the sensor with the environment in which the sensor executes.
     *
     * <p>This includes the:</p>
     *
     * @param conf The sensor site configuration of this sensor. This is the configuration provided to the sensor
     *             merged in with cluster configuration on this machine.
     * @param context This object can be used to get information about this task's place within the topology,
     *                including the task id and component id of this task, input and output information, etc.
     */
    void open(SensorContext context);

    /**
     * Called when an sensor is going to be shutdown. There is no guarentee that close
     * will be called, because the supervisor kill -9's worker processes on the cluster.
     *
     * <p>The one context where close is guaranteed to be called is a topology is
     * killed when running Storm in local mode.</p>
     */
    void close();

    /**
     * Called when a sensor has been activated out of a deactivated mode.
     * A sensor can become activated after having been deactivated when the sensor
     * clients are manipulating the sensors.
     */
    void activate();

    /**
     * Called when a sensor has been deactivated. The sensor may or may not be reactivated in the future.
     */
    void deactivate();
}
