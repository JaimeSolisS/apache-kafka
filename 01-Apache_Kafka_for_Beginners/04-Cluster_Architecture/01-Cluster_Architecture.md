# Cluster Architecture

Kafka Brokers are often configured to form a cluster. A cluster is nothing but a group of brokers that work together to share the workload, and that's how Apache Kafka becomes are distributed and scalable system. You're going to start with a single broker in your development environment and deploy a cluster of three or five brokers in your production environment. Later, as the workload grows you can increase the number of brokers in the cluster. It can grow up to hundreds of brokers in a single cluster.

However, the notion of clustering brings out two critical questions. Who manages cluster membership? And who performs the routine administrative tasks in the cluster? 

In a typical distributed system, there is a `master node` that maintains a list of active cluster members. The master always knows the state of the other members. The first question is about the master node.

Who manages the list of active brokers, and how does it know that the broker has possibly crashed or a new broker has recently joined the cluster? The second question is also related to the administrative activities that are typically performed by the master in a clustered environment.

Suppose a broker is active and it is taking care of some responsibilities in the cluster. Suddenly the broker leaves the cluster or dies for some reason. Now at this stage, Who will perform those responsibilities? We need someone to reassign that work to an active broker to ensure that the cluster continues to function.

## Zookeper

Broker is a masterless cluster. It doesn't follow a master slave architecture. However, it uses `Apache ZooKeeper` to maintain the list of active brokers. Every Kafka broker has a `unique_id` that you define in the broker configuration file. We also specify the zookeeper connection details in the broker configuration file.

When the broker starts, it connects to the zookeeper and creates an ephemeral node using broker_id to represent an active broker session. The ephemeral node remains intact as long as the broker session with the zookeeper is active. When the broker loses connectivity to the zookeeper for some reason, the zookeeper automatically removes the ephemeral node. So the list of active brokers in the cluster is maintained as the list of ephemeral nodes under the /brokers/ids path in the zookeeper. 

You can see that list by starting a zookeeper shell using this command.

```
>zookeeper-shell.bat localhost:2181
Connecting to localhost:2181
Welcome to ZooKeeper!
```
We're now connected to the zoo keeper of my Kafka Cluster. We can easily look at what we have in the zookeeper database. Let me issue the ls command.

```
> ls /
[admin, brokers, cluster, config, consumers, controller, controller_epoch, feature, isr_change_notification, latest_producer_id_block, log_dir_event_notification, zookeeper]
```

We have these many hierarchies, but we're interested in the brokers. So let's look into the ids.
```
> ls /brokers
[ids, seqid, topics]
> ls /brokers/ids
[0, 1, 2]
```
So we have 0, 1, and 2. Three brokers in this cluster. What if I bring one broker down?. Let us stop broker id 1.
```
> ls /brokers/ids
[0, 2]
```

Broker id 1 is gone from the zookeeper.  

So this is how Kafka maintains the list of brokers in a cluster. The list of active brokers in the cluster is maintained as the list of ephemeral nodes under the /brokers/ids path in the zookeeper.

## Kafka Cluster Controller

Kafka is a masterless cluster, and the list of active brokers is maintained in the zookeeper.
However, we still need someone to perform the routine administrative activities such as monitoring the list of active brokers and reassigning the work when an active broker leaves the cluster.

All those activities are performed by a `controller` in the Kafka cluster. The controller is not a master. It is simply a broker that is elected as a controller to pick up some extra responsibilities. That means, **the controller also acts as a regular broker**. So, if you have a single node cluster, it serves as a controller as well as a broker.

However, there is only one controller in the Kafka cluster at any point in time. The controller is responsible for monitoring the list of active brokers in the zookeeper. When the controller notices that a broker left the cluster, it knows that it is time to reassign some work to the other brokers. 

The controller election is is straightforward. The first broker that starts in the cluster becomes the controller by creating an ephemeral controller node in the zookeeper. When other brokers start, they also try to create this node, but they receive an exception as 'node already exists,' which means that the controller is already elected.

In that case, they start watching the controller node in the zookeeper to disappear. When the controller dies, the ephemeral node disappears. Now, every broker again tries to create the controller node in the zookeeper, but only one succeeds, and others get an exception once again. This process ensures that there is always a controller in that cluster, and there exists only one controller.

If we ls the root directory of Zookeeper, we can see that there is a controller node already created. So we have a controller elected for this cluster. And that's obvious. But who is the controller? Let's query the controller node.
```
> get /controller
{"version":1,"brokerid":0,"timestamp":"1645462800438"}
```

So broken id 0 is the controller.

What if I bring that broker down? We'll have a new controller. If broker 0 comes back, it won't become a controller since someone else is already elected as a controller.

So that's how the Kafka cluster is managed. Just to summarize, zookeeper is the database of the Kafka cluster control information. And one of the broker in the cluster is elected to take up the responsibilities of the controller and take care of the cluster level activities. 

## Partition Allocation & Fault Tolerance 

In the earlier session, we explore two different dimensions of Apache Kafka. `Log files` and `Cluster` formation. The next logical step is to tie up the relationship between these two dimensions and understand how the work is distributed among the brokers in a Kafka cluster. Primarily, we want to understand what makes Kafka is scalable and fault tolerance system.

### Kafka Fault Tolerance

If we look at the kafka topic organization, it is broken in two independent partitions. Each partition is self-contained. What I mean is simple. All the information about the partition, such as segment files and indexes are stored inside the same directory. This structure is excellent because it allows us to distribute the work among Kafka brokers in a cluster efficiently. All we need to do is to spread the responsibility of partitions in the Kafka cluster.

The point is is straightforward. When you create a topic, the responsibility to create, store, and manage partitions is distributed among the available brokers in the cluster. That means every Kafka broker in the cluster is responsible for managing one or more partitions that are assigned to that broker. And that is how the verdict is shared by the brokers.

Having said that, let's try to understand it with the help of an example. 

Kafka cluster is a group of brokers. These brokers may be running on individual machines. In a large production cluster, you might have organized those machines in multiple racks.

How are the partitions allocated to brokers? I mean, how we decide which brokers should be maintaining which partition? Are there any rules for assigning work among the brokers?

Suppose you have six brokers and you placed them into two different racks. You decide to create a topic with 10 partitions and a replication factor of three. That means, Kafka now have 30 replicas to allocate to six brokers. Are there any rules for the assignment? Well, Kafka tries to achieve two goals for this partition allocation.

- Even Distribution - Partitions are distributed evenly as much as possible to achieve work load balance 
- Fault Tolerance - Follower partitions I mean duplicate copies must be placed on different machines to achieve fault tolerance. 

That's it. Just two goals. These are not rules, but you can pick them as a goal that Kafka would want to achieve. Now let's see how it gets distributed.

To distribute our 30 partitions, Kafka applies the following is steps:

- Make an ordered list of available brokers
- Assign leaders and followers to the list in order.

Let's apply the first rule. Kafka begins with a randomly chosen broker in a rack and places it into a list. Let's assume we start with broker 0. Now we have one item on the list. The next broker in the list must be from a different rack. So, let's assume Kafka picks Broker 3. Now we have two brokers in the ordered list. The next one again comes from the first track. Let's say, this time it is broker 1. This goes on as an alternating process for selecting another broker in a different rack. So, we take broker 4, then broker 2, and finally broker 5. That's all. You have an ordered list of brokers:

R1-B0 -> R2-B0 -> R1-B0 -> R2-B0 ->R1-B0 -> R2-B0

Now we come to the second step. Assigning partitions to this list. We have 30 partitions to allocate to these six brokers. Ideally, Kafka should place five partitions on each broker to achieve the first goal of evenly distributing the partitions. However, we have one other goal to achieve fault tolerance right. 

### What is for tolerance?

It is as simple as making sure that if one broker fails for some reason, we still have a copy on some other broker. Further, making sure that if the entire rack fails, we still have a copy on a different rack.

We can achieve fault tolerance by merely placing duplicate copies on different machines. For example, the first partition has three copies. All we need to do is to make sure that these three copies are not allocated to the same broker in the above list.

| Brokers | Leaders |   | Followers  |   | Followers |   |
|---------|---------|---|------------|---|-----------|---|
| R1-BO   |         |   |            |   |           |   |
| R2-B3   |         |   |            |   |           |   |
| R1-B1   |         |   |            |   |           |   |
| R2-B4   |         |   |            |   |           |   |
| R1-B2   |         |   |            |   |           |   |
| R2-B5   |         |   |            |   |           |   |

Let us see this step by step process. Once we have the ordered list of available brokers, assigning partitions is as simple as assign one to each broker using a round robin method.

Kafka starts with the leader partitions and finishes creating all leaders first. So we take the leader of the partitions zero and assign it to broker 0. The leader of partition one goes to broker 3, the leader of partition 2 learns on broker 1 and so on.

| Brokers | Leaders |    | Followers  |  | Followers |  |
|---------|---------|----|------------|--|-----------|--|
| R1-BO   | P0      | P6 |            |  |           |  |
| R2-B3   | P1      | P7 |            |  |           |  |
| R1-B1   | P2      | P8 |            |  |           |  |
| R2-B4   | P3      | P9 |            |  |           |  |
| R1-B2   | P4      |    |            |  |           |  |
| R2-B5   | P5      |    |            |  |           |  |

Once the leader partitions are created, it starts creating the first follower. The first follower allocation simply starts from the second broker in the list and follows a round robin approach. So, we skip the first broker in the list and place the first follower of partition 0 to broker 3, the first follower of partition 1 goes to broker 1. Similarly, the first follower of partition 2 goes to broker 4, and so on others are also created.

| Brokers | Leaders |    | Followers  |    | Followers |  |
|---------|---------|----|------------|----|-----------|--|
| R1-BO   | P0      | P6 |            | P5 |           |  |
| R2-B3   | P1      | P7 | P0         | P6 |           |  |
| R1-B1   | P2      | P8 | P1         | P7 |           |  |
| R2-B4   | P3      | P9 | P2         | P8 |           |  |
| R1-B2   | P4      |    | P3         | P9 |           |  |
| R2-B5   | P5      |    | P4         |    |           |  |

Finally, it begins with the list of the second follower and maps them to the same broker list by jumping one more broker from the previous start. So the second follower of partition zero goes to broker one, and similarly, others are also created.



| Brokers | Leaders |    | Followers  |    | Followers |    |
|---------|---------|----|------------|----|-----------|----|
| R1-BO   | P0      | P6 |            | P5 |           | P4 |
| R2-B3   | P1      | P7 | P0         | P6 |           | P5 |
| R1-B1   | P2      | P8 | P1         | P7 | P0        | P6 |
| R2-B4   | P3      | P9 | P2         | P8 | P1        | P7 |
| R1-B2   | P4      |    | P3         | P9 | P2        | P8 |
| R2-B5   | P5      |    | P4         |    | P3        | P9 |

This is what happens when you created topic. Leaders and followers of the topic are created across the cluster. If you look at the outcome of this allocation, we couldn't achieve a `perfectly even distribution`. The broker 0 has got four and broker 4 has got six partitions.
However, we made an `ideal fault tolerance` at the price of little disparity. 

Let's quickly check the fault tolerance level. The leader of the fourth partition is placed at the broker 2 that sits in the first track. however, the first follower of partition 4 goes to a different track a broker 5. So even if one of the racks is entirely down, we still have at least one copy of the partition 4 available on the other. Similarly the second follower is placed at a different broker B0. So even if 2 brokers are down, we will still have at least one broker available to serve the partition 4.

You can validated with other partitions as well. They all are well arranged in a way that at least two copies are placed on two different tracks. This arrangement is ideal for fault tolerance.

So we learn how the replicas are distributed among the brokers in the cluster, making the system fault tolerant. However, distributing the replicas among the brokers is the first half of the work distribution.

## Leader vs Follower

We learned that the broker manages two types of partitions. Leader partition and Follower partition. Depending upon the partition type, a typical broker performs two kinds of activities. Leader activities and follower activities. Let us try to understand it. In the example, we allocated 30 replicas amongst six brokers. Now each broker owns multiple replicas.

For example, the broker 4 holds six replicas. Two of these replicas of the leader partitions, and the remaining four are the following partition. So, the broker acts as a leader for the two leader partitions, and it also acts as a follower for the remaining four follower partitions.

`Regarding Kafka broker, being a leader means one thing. |The leader is responsible for all the requests from the producers and consumers`. For example, let's assume that a producer wants to send some messages to a Kafka topic. So the producer will connect to one of the brokers in the cluster and query for the topic metadata. All Kafka brokers can answer the metadata request, and hence the producer can connect to any of the broker and query for that metadata. The metadata contains a list of all the leader partitions and their respective host and port information. Now the producer has a list of all leaders. It is the producer that decides on which partition does it want to send the data, and accordingly send the message to the respective broker.

That means the producer directly transmits the message to a leader. On receiving the message, the leader broker persists the message in the leader partition and sends back an acknowledgement. Similarly, when a consumer wants to read message. It always reads from the leader of the partition.

> St this stage, you should be clear that the producer and the consumer always interact with the leader. And that's what is the responsibility of the leader broker. Interact with the producer and the consumer.

Now let's come back to the follower. Kafka broker also acts as a follower for the follower partitions that are allocated to the broker. In the figure, the broker B4 owns four follower partitions and hence the B4 acts as a follower for these replicas. `Followers do not serve producer and consumer requests. Their only job is to copy messages from the leader and stay up to date with all the messages`.

The aim of the follower is to get elected as a leader when the current leader fails or dies. So, they have a single point agenda. `Stay in sync with the leader`. Because they can't get elected as a leader if they are falling behind the leader and fail to be in sync with the leader by copying all the messages. 

### How does the follower stay in sync with the leader?

To stay in sync with the leader, the follower connects to the leader and requests for the data. The leader send some messages, and the followed persists them in the replica and requests for more. This goes on forever as an infinite loop to ensure that the followers are in sync with the leader.

## the ISR List

The method of copying messages that the follower appears full proof. However, some followers can still fail to stay in sync for various reasons. Two common reasons are network congestion and broker failures.

- Network congestion can slow down replication, and the followers may start falling behind.

- When a follower broker crashes, all replicas on that broker will begin falling behind until we restart the follower broker and they can start replicating again

Since the replicas may be falling behind, the leader has one more important job to maintain a list of `In-Sync-Replicas(ISR)`. This list is known as the ISR the list of the partition and persisted in the zookeeper. And this list is maintained by the leader broker. The ISR list is very critical. `Because all the followers in that list are known to be in sync with the leader, and they are an excellent
candidate to be elected as a new leader when something wrong happens to the current leader`. And that makes the ISR list a critical thing. 
 
### how do a leader would know if the follower is in sync or still lagging?

The follower will connect to the leader and request for the messages. The first request would ask the leader to send messages is starting from the offset zero. Assume that the leader has got 10 messages, so it sends all of them to the follower. The follower restores them and do the replica and again requests for new messages starting from offset 10. In this case, since the follower asked for offset 10, that means a leader can safely assume that the follower has already persisted all the earlier messages. So by looking at the last offsets requested by the follower, the leader can tell how far behind is the replica.

Now the ISR list is easy to maintain. If the replica is 'not too far,' the leader will add the follower to the ISR list, or else the followed is removed from the ISR list. That means the ISR list is dynamic and the followers keep getting added and removed from the ISR list depending on how far they maintain their in0sync status.

### How do we define the 'not too far?'

As a matter of fact the follower will always be a little behind and the leader, and that's obvious because follower needs to ask for the message from the leader, received the message over the network, store them into the replica and then ask for more. All these activities take some time. And hence, the leader gives them some minimum time as a margin to accomplish this.

That is where the notion of 'not too far' arrives. `The default value of 'not too far' is 10 seconds`. However, you can increase or decrease it using Kafka configurations. So the replica is kept in the ISR list if they are not more than 10 seconds behind the leader. That means, if the replica has requested the most recent message in the last 10 seconds, they deserve to be in the ISR. If not, the leader removes the replica from the ISR list.

## Committed vs Un-committed Messages

The mechanism to maintain the ISR list is quite fancy. However, we still have a gotcha in this mechanism. The maintenance of the ISR list leads to some new concepts. We already understand that the followers may be lagging behind the leader for several reasons and they might get removed from the ISR.

Assume all the followers in the ISR are 11 seconds behind the leader. That means none of them qualified to be in the ISR. So, your ISR list becomes logically empty. The messages are only at the leader, right? Now for some reason, the leader crashed, and we need to elect a new leader.

Who do we choose? If we elect a new leader among the followers that are not in the ISR, don't we lose those messages that are collected at the leader during the most recent 11 seconds? Yes, we miss them, but how do we handle that?

The solution is simple which is implemented using two concepts.

- The first idea is to introduce the notion of `committed and uncommitted messages`.
- The second idea is to certain minimum in sync replicas configuration.

You can configure the leader to not to consider a` message committed` until the message is copied at all the followers in the ISR list. If you do that, the leader may have some committed as well as some uncommitted messages. The message is committed when it is safely copied at all replicas in the ISR. Now, if the message is committed, we cannot lose it until we lose all the replicas. So committed messages are now taken care of.

However, if we lose the leader, we still miss the uncommitted messages. But the uncommitted messages shouldn't be a worry, because those can be resent by the producer. Because producers can choose to receive acknowledgement of sent messages only after the message is fully committed. In that case, `the producer waits for the acknowledgement for a timeout period and resends the messages in the absence of commit acknowledgement`. So, the uncommitted messages are lost at the failing leader, but the newly elected leader will receive those messages again from the producer. That's how all the messages can be protected from loss.

## The minimun In-Sync Replicas

The data is considered committed when it is written to all the in-sync replicas.

Now let's assume that we start with three replicas and all of them are healthy enough to be in the ISR. However, after some time, two of them failed, and as a result of that, the leader will remove them from the ISR. In this case, even though we configured the topic to have three replicas, we are left with the single in-sync replica that is the leader itself.

Now the data is considered committed when it is written to all in-sync replicas, even when all means just one replica(the leader itself). It is a risky scenario for data consistency because data could be lost if we lose the leader. Kafka protects this scenario by setting the minimum number of in-sync replicas for a topic. If you would like to be sure that committed data is written to at least two replicas, you need to set the minimum number of in-sync replicas as two.

However, there is a side effect of this setting. If a topic has three replicas and you set minimum in-sync replicas to two, then you can only write to a partition in the topic if at least two out of the three replicas are in-sync. When at least two replicas are not in sync, the broker will not accept any messages and respond with 'not enough replicas' exception.

In other words, the leader becomes a read only partition. You can read, but you cannot write until you bring another replica online and wait for it to catch up and get in sync.
