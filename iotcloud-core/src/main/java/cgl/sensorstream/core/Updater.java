package cgl.sensorstream.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.concurrent.BlockingQueue;

public class Updater implements MessageListener {
    private static Logger LOG = LoggerFactory.getLogger(Updater.class);

    private BlockingQueue<Update> updates;

    public Updater(BlockingQueue<Update> updates) {
        this.updates = updates;
    }

    @Override
    public void onMessage(Message message) {
        // parse the update message and put it to the queue
        Update update = new Update(Update.Type.ADD, "/");
        try {
            updates.put(update);
        } catch (InterruptedException e) {
            LOG.error("Error occurred while putting the update to queue");
        }
    }
}
