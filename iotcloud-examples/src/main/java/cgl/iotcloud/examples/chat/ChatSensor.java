package cgl.iotcloud.examples.chat;

import cgl.iotcloud.core.Configurator;
import cgl.iotcloud.core.ISensor;
import cgl.iotcloud.core.SensorContext;
import cgl.iotcloud.core.SensorId;
import cgl.iotcloud.core.sensorsite.SiteContext;
import cgl.iotcloud.core.transport.Channel;
import cgl.iotcloud.core.transport.Direction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Message;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ChatSensor implements ISensor {
    private static Logger LOG = LoggerFactory.getLogger(ChatSensor.class);

    private SensorContext context;

    @Override
    public Configurator getConfigurator(Map conf) {
        return null;
    }

    @Override
    public void open(SensorContext context) {
        this.context = context;
        LOG.info("Received open request {}", this.context.getId());
    }

    @Override
    public void close() {

    }

    @Override
    public void activate() {
        LOG.info("Received activation request {}", this.context.getId());
    }

    @Override
    public void deactivate() {
        LOG.info("Received de-activation request {}", this.context.getId());
    }

    private class ChatConfigurator implements Configurator {
        @Override
        public SensorContext configure(SiteContext siteContext) {
            SensorContext context = new SensorContext(new SensorId("chat", "general"));

            BlockingQueue<Message> inMassages = new ArrayBlockingQueue<Message>(1024);
            BlockingQueue<Message> outMassages = new ArrayBlockingQueue<Message>(1024);

            Channel<Message, Message> channel = new Channel<Message, Message>(Direction.IN, new HashMap(), inMassages, outMassages);

            return null;
        }
    }
}
