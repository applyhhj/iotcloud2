package cgl.iotcloud.transport.kafka.consumer;

import java.util.List;

public interface PartitionCoordinator {
    List<PartitionManager> getMyManagedPartitions();

    PartitionManager getManager(Partition partition);

    void refresh();
}
