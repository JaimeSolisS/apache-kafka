# Consumer positions

So, the problem of scalability for the consumers is taken care of by the consumer groups. The issue of fault tolerance is also taken care of by the rebalancing within the consumer groups. However, we still have an open question.

Assume that the partition was initially assigned to a consumer. The consumer processed some messages for a while and crashed. Kafka automatic rebalancing will detect the failure of the consumer and reassign the unattended partition to some other consumer in the group.

The new consumer should not reprocess the events that are already processed by the earlier consumer before it failed. How would Kafka handle this?

## Consumer Current Position?

We already learned  earlier  that an offset uniquely identifies every message in a partition. Kafka also maintains two offset positions for each partition. Current offset position and Committed offset position. These positions are maintained for the consumer.

The Current offset position of the consumer is the offset of the next record that will be given out to the consumer for the next poll(). In the beginning, the current offset might be unknown or null for a new consumer subscription. In that case, you can set the auto-offset-reset configuration to the earliest or latest. If you set to earliest, then Kafka will set the current offset position to the first record in the partition and start giving you all the messages from the beginning. Otherwise, the default value is the latest, which will send you only upcoming messages after the consumer subscribed and ignore all earlier messages. This all happens only in the beginning when the current-offset, as well as the committed-offset, is undefined.

Once the initial current-offset is determined, it automatically advances every time you poll() some messages. The current offset is persistent to the consumer session. If the consumer fails or restarts, then the current offset is determined once again. For that reason, if you restart a consumer after a failure, you may start getting the records once again that were already sent in the earlier session.

To avoid that situation, Kafka also maintains a committed offset position. Every time you poll(), the consumer will automatically commit the earlier current-offset and send some more records. These new records are then automatically committed by the next poll(). This mechanism is known as auto-commit. The committed offset position is the last offset that has been stored securely at the broker. So, when the consumer process fails and restarts, or the partition is reassigned to some other consumer in the group, the committed offset is used to override the current offset position for the consumer.

So, in summary, the committed offset is securely stored with the broker. When a consumer restart or the partition is reassigned to another consumer, the committed-offset is used to avoid duplicate processing. And all this happens automatically in most of the cases. But you also have options to take control in your hand and do it manually using commit APIs.

The current offset is determined as latest or earliest only when there is no committed offset. Otherwise, the committed-offset is used to set the current offset.