package cgl.iotcloud.core;

import java.util.concurrent.BlockingQueue;

public interface MessageSender {
    boolean loop(BlockingQueue queue);
}
