package cgl.iotcloud.transport.kafka.consumer;

public interface IBrokerReader {
    GlobalPartitionInformation getCurrentBrokers();
    void close();
}
