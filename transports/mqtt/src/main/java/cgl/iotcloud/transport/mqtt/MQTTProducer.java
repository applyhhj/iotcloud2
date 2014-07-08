package cgl.iotcloud.transport.mqtt;

import org.fusesource.mqtt.client.*;
import org.fusesource.mqtt.codec.MQTTFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;

public class MQTTProducer {
    private static Logger LOG = LoggerFactory.getLogger(MQTTProducer.class);

    private CallbackConnection connection;

    private String url;

    private int port;

    private BlockingQueue messages;

    private String queueName;

    private boolean trace = false;

    private QoS qoS;

    private boolean run = true;

    public MQTTProducer(String url, int port, BlockingQueue messages, String queueName) {
        this(url, port, messages, queueName, QoS.AT_MOST_ONCE);
    }

    public MQTTProducer(String url, int port, BlockingQueue messages, String queueName, QoS qoS) {
        this.url = url;
        this.messages = messages;
        this.queueName = queueName;
        this.qoS = qoS;
        this.port = port;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    private enum State {
        INIT,
        CONNECTED,
        TOPIC_CONNECTED,
        DISCONNECTED,
    }

    private State state = State.INIT;

    public void open() {
        MQTT mqtt = new MQTT();

        try {
            if (port != -1) {
                mqtt.setHost(url, port);
            } else {
                mqtt.setHost(url);
            }
        } catch (URISyntaxException e) {
            String msg = "Invalid URL for the MQTT Broker";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        if (trace) {
            mqtt.setTracer(new Tracer() {
                @Override
                public void onReceive(MQTTFrame frame) {
                    LOG.info("recv: " + frame);
                }

                @Override
                public void onSend(MQTTFrame frame) {
                    LOG.info("send: " + frame);
                }

                @Override
                public void debug(String message, Object... args) {
                    LOG.info(String.format("debug: " + message, args));
                }
            });
        }

        connection = mqtt.callbackConnection();

        connection.getDispatchQueue().execute(new Runnable() {
            @Override
            public void run() {
                connection.connect(new Callback<Void>() {
                    public void onFailure(Throwable value) {
                        state = State.INIT;
                        String s = "Failed to connect to the broker";
                        LOG.error(s, value);
                        throw new RuntimeException(s, value);
                    }

                    // Once we connect..
                    public void onSuccess(Void v) {
                        LOG.debug("Connection established");
                        state = State.CONNECTED;
                    }
                });
            }
        });

        Thread workerThread = new Thread(new Worker());
        workerThread.start();
    }

    public void close() {
        run = false;
        if (connection != null && (state == State.CONNECTED || state == State.TOPIC_CONNECTED)) {
            // To disconnect..
            connection.getDispatchQueue().execute(new Runnable() {
                @Override
                public void run() {
                    connection.disconnect(new Callback<Void>() {
                        public void onSuccess(Void v) {
                            // called once the connection is disconnected.
                            state = State.DISCONNECTED;
                        }
                        public void onFailure(Throwable value) {
                            // Disconnects never fail.
                            state = State.DISCONNECTED;
                        }
                    });
                }
            });

        }
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            int errorCount = 0;
            while (run) {
                try {
                    if (state == State.CONNECTED) {
                        try {
                            final Object input = messages.take();
                            connection.getDispatchQueue().execute(new Runnable() {
                                @Override
                                public void run() {
                                    if (input instanceof byte []) {
                                        connection.publish(queueName, (byte []) input, qoS, false, null);
                                    } else {
                                        throw new RuntimeException("Expepected byte array after conversion");
                                    }
                                }
                            });
                        } catch (InterruptedException e) {
                            LOG.error("Exception occurred in the worker listening for consumer changes", e);
                        }
                    } else {
                        Thread.sleep(100);
                    }
                } catch (Throwable t) {
                    errorCount++;
                    if (errorCount <= 3) {
                        LOG.error("Error occurred " + errorCount + " times.. trying to continue the worker", t);
                    } else {
                        LOG.error("Error occurred " + errorCount + " times.. terminating the worker", t);
                        run = false;
                    }
                }
            }
            String message = "Unexpected notification type";
            LOG.error(message);
            throw new RuntimeException(message);
        }
    }

}
