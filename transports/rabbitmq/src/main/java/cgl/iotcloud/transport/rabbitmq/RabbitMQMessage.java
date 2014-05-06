package cgl.iotcloud.transport.rabbitmq;

import com.rabbitmq.client.AMQP;

public class RabbitMQMessage {
    private AMQP.BasicProperties basicProperties;

    private byte[] body;

    public RabbitMQMessage(AMQP.BasicProperties basicProperties) {
        this.basicProperties = basicProperties;
    }

    public RabbitMQMessage(byte[] body) {
        this.body = body;
    }

    public RabbitMQMessage(AMQP.BasicProperties basicProperties, byte[] body) {
        this.basicProperties = basicProperties;
        this.body = body;
    }

    public AMQP.BasicProperties getBasicProperties() {
        return basicProperties;
    }

    public byte[] getBody() {
        return body;
    }
}
