package cgl.iotcloud.transport.mqtt;

import cgl.iotcloud.core.transport.MessageConverter;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.*;
import org.fusesource.mqtt.codec.MQTTFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class MQTTConsumer {
    private static Logger LOG = LoggerFactory.getLogger(MQTTConsumer.class);

    private CallbackConnection connection;

    private String url;

    private int port;

    private BlockingQueue messages;

    private String queueName;

    private boolean trace = false;

    private QoS qoS;

    private MessageConverter converter;

    public MQTTConsumer(String url, int port, BlockingQueue messages, String queueName, MessageConverter converter) {
        this(url, port, messages, queueName, converter, QoS.AT_MOST_ONCE);
    }

    public MQTTConsumer(String url, int port, BlockingQueue messages, String queueName, MessageConverter converter, QoS qoS) {
        this.url = url;
        this.messages = messages;
        this.queueName = queueName;
        this.qoS = qoS;
        this.converter = converter;
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
        // Start add a listener to process subscription messages, and start the
        // resume the connection so it starts receiving messages from the socket.
        connection.listener(new Listener() {
            public void onConnected() {
                LOG.debug("connected");
            }

            public void onDisconnected() {
                LOG.debug("disconnected");
            }

            public void onPublish(UTF8Buffer topic, Buffer payload, Runnable onComplete) {
                final String uuid = UUID.randomUUID().toString();
                try {
                    MQTTMessage message = new MQTTMessage(payload, topic.toString());
                    Object converted = converter.convert(message, null);
                    messages.put(converted);
                    onComplete.run();
                } catch (InterruptedException e) {
                    LOG.error("Failed to put the message to queue", e);
                }
            }

            public void onFailure(Throwable value) {
                LOG.warn("Connection failure: {}", value);
                connection.disconnect(null);
                state = State.DISCONNECTED;
            }
        });

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
                        connection.getDispatchQueue().execute(new Runnable() {
                            @Override
                            public void run() {
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
                    }
                });
            }
        });
    }

    public void close() {
        if (connection != null && (state == State.CONNECTED || state == State.TOPIC_CONNECTED)) {
            connection.getDispatchQueue().execute(new Runnable() {
                @Override
                public void run() {
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
            });
        }
    }
}
