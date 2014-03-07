package cgl.sensorstream.storm;

import backtype.storm.contrib.jms.DestinationChanger;
import backtype.storm.contrib.jms.Notification;
import cgl.sensorstream.core.Utils;
import cgl.sensorstream.core.config.Configuration;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ZooKeeperDestinationChanger implements DestinationChanger {
    private static Logger LOG = LoggerFactory.getLogger(ZooKeeperDestinationChanger.class);

    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection = null;
    private Session session;

    private BlockingQueue<Notification> notifications;

    private CuratorFramework client = null;
    private PathChildrenCache cache = null;

    public void start(Map conf) {
        // Create a ConnectionFactory
        try {
            connectionFactory = new ActiveMQConnectionFactory((String) conf.get(Configuration.SS_BROKER_URL));
            connection = connectionFactory.createConnection();
            connection.start();
            // Create a Session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            String msg = "Error connecting to the broker";
            LOG.error(msg);
            throw new RuntimeException(msg, e);
        }

        notifications = new ArrayBlockingQueue<Notification>((Integer)
                conf.get(Configuration.SS_SENSOR_UPDATES_SIZE));

        client = CuratorFrameworkFactory.newClient(Utils.getZkConnectionString(conf),
                new ExponentialBackoffRetry(1000, 3));
        client.start();

        // in this example we will cache data. Notice that this is optional.
        cache = new PathChildrenCache(client, Configuration.getZkRoot(conf), true);
        try {
            cache.start();
        } catch (Exception e) {
            String msg = "Failed to start the path cache";
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        addListener(cache);
    }

    private void addListener(PathChildrenCache cache) {
        // a PathoeyabsoyydrafwigChildrenCacheListener is optional. Here, it's used just to log changes
        PathChildrenCacheListener listener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED: {
                        Notification notification = new Notification(
                                event.getData().getPath(),
                                createDestination(getDestinationPath(event.getData().getData())),
                                Notification.Type.ADD);
                        notifications.put(notification);
                        break;
                    }
                    case CHILD_UPDATED: {
                        LOG.debug("Received update notification, ignoring..: ", event.getData().getPath());
                        break;
                    }
                    case CHILD_REMOVED: {
                        Notification notification = new Notification(
                                event.getData().getPath(),
                                createDestination(getDestinationPath(event.getData().getData())),
                                Notification.Type.REMOVE);
                        notifications.put(notification);
                        break;
                    }
                }
            }
        };
        cache.getListenable().addListener(listener);
    }

    private String getDestinationPath(byte []data) {
        throw new NotImplementedException();
    }

    private Destination createDestination(String path) {
        // Create a Connection
        try {

            // Create the destination (Topic or Queue)
            Destination destination = session.createQueue(path);

            return destination;
        } catch (JMSException e) {
            String msg = "Error creating the JMS Destination: " + path;
            LOG.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public BlockingQueue<Notification> getNotifications() {
        return notifications;
    }

    public void stop() {
        CloseableUtils.closeQuietly(cache);
        CloseableUtils.closeQuietly(client);
    }
}
