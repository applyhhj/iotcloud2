package cgl.iotcloud.transport.mqtt;

import cgl.iotcloud.core.transport.MessageConverter;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
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

    private BlockingQueue messages;

    private String queueName;

    private boolean trace = false;

    private QoS qoS;

    private MessageConverter converter;

    public MQTTProducer(String url, BlockingQueue messages, String queueName, MessageConverter converter) {
        this(url, messages, queueName, converter, QoS.AT_MOST_ONCE);
    }

    public MQTTProducer(String url, BlockingQueue messages, String queueName, MessageConverter converter, QoS qoS) {
        this.url = url;
        this.messages = messages;
        this.queueName = queueName;
        this.qoS = qoS;
        this.converter = converter;
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
            mqtt.setHost(url);
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

        connection.connect(new Callback<Void>() {
            public void onFailure(Throwable value) {
                state = State.INIT;
                String s = "Failed to connect to the broker";
                LOG.error(s, value);
                throw new RuntimeException(s, value);
            }

            // Once we connect..
            public void onSuccess(Void v) {
                // Subscribe to a topic
                Topic[] topics = {new Topic(queueName, qoS)};
                connection.subscribe(topics, new Callback<byte[]>() {
                    public void onSuccess(byte[] qoses) {
                        // The result of the subcribe request.
                        LOG.debug("Subscribed to the topic {}", queueName);
                        state = State.TOPIC_CONNECTED;
                    }

                    public void onFailure(Throwable value) {
                        LOG.error("Failed to subscribe to topic", value);
                        connection.disconnect(null);
                        state = State.DISCONNECTED;
                    }
                });
            }
        });

        Thread workerThread = new Thread(new Worker());
        workerThread.start();
    }

    public void close() {
        if (connection != null && (state == State.CONNECTED || state == State.TOPIC_CONNECTED)) {
            // To disconnect..
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
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            boolean run = true;
            int errorCount = 0;
            while (run) {
                try {
                    if (state == State.TOPIC_CONNECTED) {
                        try {
                            Object input = messages.take();
                            Object converted = converter.convert(input, null);
                            if (converted instanceof byte []) {
                                connection.publish(queueName, (byte [])converted, qoS, false, null);
                            } else {
                                throw new RuntimeException("Expepected byte array after conversion");
                            }
                        } catch (InterruptedException e) {
                            LOG.error("Exception occurred in the worker listening for consumer changes", e);
                        }
                    } else {
                        Thread.sleep(100);
                    }
                } catch (Throwable t) {
                    errorCount++;
                    if (errorCount <= 3) {
                        LOG.error("Error occurred " + errorCount + " times.. trying to continue the worker");
                    } else {
                        LOG.error("Error occurred " + errorCount + " times.. terminating the worker");
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
