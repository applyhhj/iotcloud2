package cgl.iotcloud.transport.kafka;

import cgl.iotcloud.core.transport.MessageConverter;
import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.ErrorMapping;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class KafkaConsumer {
    private static Logger LOG = LoggerFactory.getLogger(KafkaConsumer.class);

    private String topic;
    private int partition;
    private Map<String, Integer> seedBrokers;
    private Map<String, Integer> m_replicaBrokers = new HashMap<String, Integer>();
    private int soTimeout = 30000;
    private int bufferSize = 64 * 1024;
    private int fetchSize = 10000;

    private int pollingInterval = 10;

    private MessageConverter converter;

    private BlockingQueue inQueue;

    public KafkaConsumer(MessageConverter converter, BlockingQueue inQueue, String topic,
                         int partition, Map<String, Integer> seedBrokers) {
        this.topic = topic;
        this.partition = partition;
        this.seedBrokers = seedBrokers;

        this.converter = converter;
        this.inQueue = inQueue;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void start() {
        Thread t = new Thread(new Worker());
        t.start();
    }

    public void stop() {

    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            // find the meta data about the topic and partition we are interested in
            PartitionMetadata metadata = findLeader(seedBrokers, topic, partition);
            if (metadata == null) {
                throw new RuntimeException("Can't find metadata for Topic and Partition");
            }

            if (metadata.leader() == null) {
                throw new RuntimeException("Can't find Leader for Topic and Partition");
            }

            String leadBroker = metadata.leader().host();
            int leadPort = metadata.leader().port();
            String clientName = "Client_" + topic + "_" + partition;

            SimpleConsumer consumer = new SimpleConsumer(leadBroker, leadPort, soTimeout, bufferSize, clientName);
            long readOffset = getLastOffset(consumer, topic, partition, kafka.api.OffsetRequest.EarliestTime(), clientName);

            int numErrors = 0;
            while (true) {
                if (consumer == null) {
                    consumer = new SimpleConsumer(leadBroker, leadPort, soTimeout, bufferSize, clientName);
                }
                FetchRequest req = new FetchRequestBuilder()
                        .clientId(clientName)
                        .addFetch(topic, partition, readOffset, fetchSize)
                        .build();
                FetchResponse fetchResponse = consumer.fetch(req);

                if (fetchResponse.hasError()) {
                    numErrors++;
                    // Something went wrong!
                    short code = fetchResponse.errorCode(topic, partition);
                    LOG.warn("Error fetching data from the Broker:" + leadBroker + " Reason: " + code);
                    if (numErrors > 5) break;
                    if (code == ErrorMapping.OffsetOutOfRangeCode()) {
                        // We asked for an invalid offset. For simple case ask for the last element to reset
                        readOffset = getLastOffset(consumer, topic, partition, kafka.api.OffsetRequest.LatestTime(), clientName);
                        continue;
                    }
                    consumer.close();
                    consumer = null;
                    leadBroker = findNewLeader(leadBroker, topic, partition, leadPort);
                    continue;
                }
                numErrors = 0;

                long numRead = 0;
                for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(topic, partition)) {
                    long currentOffset = messageAndOffset.offset();
                    if (currentOffset < readOffset) {
                        System.out.println("Found an old offset: " + currentOffset + " Expecting: " + readOffset);
                        continue;
                    }
                    readOffset = messageAndOffset.nextOffset();
                    ByteBuffer payload = messageAndOffset.message().payload();

                    byte[] bytes = new byte[payload.limit()];
                    payload.get(bytes);
                    numRead++;

                    Object converted = converter.convert(bytes, null);

                    try {
                        inQueue.put(converted);
                    } catch (InterruptedException e) {
                    }
                }

                if (numRead == 0) {
                    try {
                        Thread.sleep(pollingInterval);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            if (consumer != null) consumer.close();
        }
    }

    public static long getLastOffset(SimpleConsumer consumer, String topic, int partition,
                                     long whichTime, String clientName) {
        TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
        kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(
                requestInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
        OffsetResponse response = consumer.getOffsetsBefore(request);

        if (response.hasError()) {
            throw new RuntimeException("Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition));
        }
        long[] offsets = response.offsets(topic, partition);
        return offsets[0];
    }

    private String findNewLeader(String a_oldLeader, String a_topic, int a_partition, int a_port) {
        for (int i = 0; i < 3; i++) {
            PartitionMetadata metadata = findLeader(m_replicaBrokers, a_topic, a_partition);
            if (metadata == null) {
            } else if (metadata.leader() == null) {
            } else if (a_oldLeader.equalsIgnoreCase(metadata.leader().host()) && i == 0) {
                // first time through if the leader hasn't changed give ZooKeeper a second to recover
                // second time, assume the broker did recover before failover, or it was a non-Broker issue
            } else {
                return metadata.leader().host();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
        System.out.println("Unable to find new leader after Broker failure. Exiting");
        throw new RuntimeException("Unable to find new leader after Broker failure. Exiting");
    }

    private PartitionMetadata findLeader(Map<String, Integer> a_seedBrokers, String a_topic, int a_partition) {
        PartitionMetadata returnMetaData = null;
        loop:
        for (Map.Entry<String, Integer> seed : a_seedBrokers.entrySet()) {
            SimpleConsumer consumer = null;
            try {
                consumer = new SimpleConsumer(seed.getKey(), seed.getValue(), soTimeout, bufferSize, "leaderLookup");
                List<String> topics = Collections.singletonList(a_topic);
                TopicMetadataRequest req = new TopicMetadataRequest(topics);
                TopicMetadataResponse resp = consumer.send(req);

                List<TopicMetadata> metaData = resp.topicsMetadata();
                for (TopicMetadata item : metaData) {
                    for (PartitionMetadata part : item.partitionsMetadata()) {
                        if (part.partitionId() == a_partition) {
                            returnMetaData = part;
                            break loop;
                        }
                    }
                }
            } catch (Exception e) {
                LOG.warn("Error communicating with Broker [" + seed + "] to find Leader for [" + a_topic
                        + ", " + a_partition + "] Reason: " + e);
            } finally {
                if (consumer != null) consumer.close();
            }
        }
        if (returnMetaData != null) {
            m_replicaBrokers.clear();
            for (kafka.cluster.Broker replica : returnMetaData.replicas()) {
                m_replicaBrokers.put(replica.host(), replica.port());
            }
        }
        return returnMetaData;
    }
}
