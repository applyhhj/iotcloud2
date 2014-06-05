package cgl.iotcloud.transport.kafka;

public class KafkaMessage {
    private String topic;

    private int partition;

    private byte[] data;

    public KafkaMessage(String topic, int partition, byte[] data) {
        this.topic = topic;
        this.partition = partition;
        this.data = data;
    }

    public String getTopic() {
        return topic;
    }

    public int getPartition() {
        return partition;
    }

    public byte[] getData() {
        return data;
    }
}
