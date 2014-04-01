package cgl.iotcloud.transport.kafka;

import cgl.iotcloud.core.transport.MessageConverter;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;

public class KafkaProducer {
    private static Logger LOG = LoggerFactory.getLogger(KafkaProducer.class);

    private Producer<String, String> producer;

    private MessageConverter converter;

    private BlockingQueue outQueue;

    private String topic;

    private String brokerList;

    private String serializerClass;

    private String partitionClass;

    private String requestRequiredAcks;

    public KafkaProducer(MessageConverter converter, BlockingQueue outQueue,
                         String topic, String brokerList, String serializerClass,
                         String partitionClass, String requestRequiredAcks) {
        this.converter = converter;
        this.outQueue = outQueue;
        this.topic = topic;
        this.brokerList = brokerList;
        this.serializerClass = serializerClass;
        this.partitionClass = partitionClass;
        this.requestRequiredAcks = requestRequiredAcks;
    }

    public void start() {
        Properties props = new Properties();
        props.put("metadata.broker.list", brokerList);
        props.put("serializer.class", serializerClass);
        props.put("partitioner.class", partitionClass);
        props.put("request.required.acks", requestRequiredAcks);
        ProducerConfig config = new ProducerConfig(props);
        producer = new Producer<String, String>(config);

        Thread t = new Thread(new Worker());
        t.start();
    }

    public void stop() {

    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            boolean run = true;
            int errorCount = 0;
            while (run) {
                try {
                    try {
                        Object input = outQueue.take();
                        Object converted = converter.convert(input, null);

                        KeyedMessage<String, String> data = new KeyedMessage<String, String>(topic, "key", (String) converted);
                        producer.send(data);
                    } catch (InterruptedException e) {
                        LOG.error("Exception occurred in the worker listening for consumer changes", e);
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
