package cgl.iotcloud.transport.kafka;

public class KafkaMessage {
    private String topic;

    private int partition;

    private byte[] data;

    private String key;

    public KafkaMessage(String topic, int partition, byte[] data) {
        this.topic = topic;
        this.partition = partition;
        this.data = data;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
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
