package cgl.iotcloud.transport.kafka;

import cgl.iotcloud.core.msg.MessageContext;
import cgl.iotcloud.core.transport.*;
import cgl.iotcloud.transport.kafka.consumer.ConsumerConfig;
import cgl.iotcloud.transport.kafka.consumer.KConsumer;
import cgl.iotcloud.transport.kafka.consumer.ZkHosts;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class KafkaTransport extends AbstractTransport {
    private static Logger LOG = LoggerFactory.getLogger(KafkaTransport.class);

    public static final int KAFKA_DEFAULT_PORT = 2012;

    public static final String PROP_URLS = "urls";

    public static final String PROP_MAX_READS = "maxReads";
    public static final String PROP_TOPIC = "topic";
    public static final String PROP_PARTITION = "partition";
    public static final String PROP_BUFFER_SIZE = "bufferSize";
    public static final String PROP_FETCH_SIZE = "fetchSize";
    public static final String PROP_POLLING_INTERVAL = "pollingInterval";

    public static final String PROP_SERIALIZER_CLASS = "serializerClass";
    public static final String PROP_PARTITION_CLASS = "partitionClass";
    public static final String PROP_REQUEST_REQUIRED_ACKS = "rrAcks";

    private Map<String, Integer> urls = new HashMap<String, Integer>();

    @Override
    public void configureTransport() {
        for (BrokerHost o : brokerHosts) {
            String url = o.getUrl();
            String tokens[] = url.split(":");
            if (tokens.length == 2) {
                urls.put(tokens[0], Integer.parseInt(tokens[1]));
            } else {
                urls.put(tokens[0], KAFKA_DEFAULT_PORT);
            }
        }
    }

    @Override
    public Manageable registerProducer(BrokerHost host, String prefix, Map channelConf, BlockingQueue<MessageContext> queue) {
        LOG.info("Registering producer to host {}", host);
        String topic = (String) channelConf.get(PROP_TOPIC);
        String serializerClass = (String) channelConf.get(PROP_SERIALIZER_CLASS);
        String partitionClass = (String) channelConf.get(PROP_PARTITION_CLASS);
        String requestRequiredAcks = (String) channelConf.get(PROP_REQUEST_REQUIRED_ACKS);

        StringBuilder brokerList = new StringBuilder();
        int count = 0;
        for (Map.Entry<String, Integer> e : urls.entrySet()) {
            if (count == urls.entrySet().size() - 1) {
                brokerList.append(e.getKey()).append(":").append(e.getValue());
            } else {
                brokerList.append(e.getKey()).append(":").append(e.getValue()).append(',');
            }
        }

        return new KafkaProducer(queue, prefix + "." +  topic, brokerList.toString(),
                serializerClass, partitionClass, requestRequiredAcks, siteId);
    }

    @Override
    public Manageable registerConsumer(BrokerHost host, String prefix, Map channelConf, BlockingQueue<MessageContext> queue) {
        LOG.info("Registering consumer to host {}", host);
        String topic = (String) channelConf.get(PROP_TOPIC);
        int partition = (Integer) channelConf.get(PROP_PARTITION);
//      return new KafkaConsumer(queue, siteId + "." + topic, partition, urls);
        ZkHosts zkHosts = new ZkHosts("localhost:2181", "/brokers");
        ConsumerConfig consumerConfig = new ConsumerConfig(zkHosts, prefix + "." + topic, "/broker", siteId + "." + prefix + "topic");
        consumerConfig.zkServers = Lists.newArrayList("localhost:2181");
        return new KConsumer(siteId, queue, consumerConfig);
    }
}
