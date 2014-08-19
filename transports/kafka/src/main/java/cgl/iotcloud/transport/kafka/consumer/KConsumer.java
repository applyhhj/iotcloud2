package cgl.iotcloud.transport.kafka.consumer;

import cgl.iotcloud.core.msg.MessageContext;
import com.google.common.base.Joiner;
import kafka.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;

public class KConsumer {
    private static Logger LOG = LoggerFactory.getLogger(KConsumer.class);

    public static class MessageAndRealOffset {
        public Message msg;
        public long offset;

        public MessageAndRealOffset(Message msg, long offset) {
            this.msg = msg;
            this.offset = offset;
        }
    }

    public static enum EmitState {
        EMITTED_MORE_LEFT,
        EMITTED_END,
        NO_EMITTED
    }

    private BlockingQueue<MessageContext> messageContexts;

    private String _uuid = UUID.randomUUID().toString();

    ConsumerConfig _spoutConfig;
    PartitionCoordinator _coordinator;
    DynamicPartitionConnections _connections;
    ZkState _state;

    long _lastUpdateMs = 0;
    int _currPartitionIndex = 0;

    String _sensor;

    public KConsumer(String _sensor) {
        this._sensor = _sensor;
    }

    public void open(Map conf) {
        Map stateConf = new HashMap(conf);
        List<String> zkServers = _spoutConfig.zkServers;
        String servers = Joiner.on(",").join(zkServers);
        _state = new ZkState(stateConf, servers, _spoutConfig.zkRoot);
        _connections = new DynamicPartitionConnections(_spoutConfig, KafkaUtils.makeBrokerReader(conf, _spoutConfig));

        // using TransactionalState like this is a hack
        _coordinator = new ZkCoordinator(_connections, conf, _spoutConfig, _state, 0, 10, _uuid, _sensor);
    }

    public void close() {
        _state.close();
    }

    public void nextTuple() {
        List<PartitionManager> managers = _coordinator.getMyManagedPartitions();
        for (int i = 0; i < managers.size(); i++) {
            try {
                // in case the number of managers decreased
                _currPartitionIndex = _currPartitionIndex % managers.size();
                EmitState state = managers.get(_currPartitionIndex).next(messageContexts);
                if (state != EmitState.EMITTED_MORE_LEFT) {
                    _currPartitionIndex = (_currPartitionIndex + 1) % managers.size();
                }
                if (state != EmitState.NO_EMITTED) {
                    break;
                }
            } catch (FailedFetchException e) {
                LOG.warn("Fetch failed", e);
                _coordinator.refresh();
            }
        }

        long now = System.currentTimeMillis();
        if ((now - _lastUpdateMs) > _spoutConfig.stateUpdateIntervalMs) {
            commit();
        }
    }

    public void deactivate() {
        commit();
    }

    private void commit() {
        _lastUpdateMs = System.currentTimeMillis();
        for (PartitionManager manager : _coordinator.getMyManagedPartitions()) {
            manager.commit();
        }
    }
}
