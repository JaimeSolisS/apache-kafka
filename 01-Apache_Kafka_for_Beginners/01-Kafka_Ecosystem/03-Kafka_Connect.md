# Kafka Connect
Kafka Connect is `a system which you can place in between your data source and the Kafka Cluster`. Then all you do is to configure it to consume data from this source system and send it to the Kafka Cluster. **You do not need to write a single line of code**. You'll just configure and run, and the Kafka Connect will do the job for you. You can also `place the Kafka Connect betwen the Cluster and the target system`.

![Kafka Connect Architecture](https://cdn.confluent.io/wp-content/uploads/Kafka_connect-1024x436.jpg)

The left side of the connector is called **Kafka Connect Source connector**. And the right side of the connector is called a **Kafka Connect Sink Connector**.

We use the source connector to pull data from a source system and send it to the Kafka Cluster. The Source Connector will internally use the Kafka producer API. 

Similarly, we use the Sink connector to consume the data from Kafka topic and Sink it to an external system. These Sink connectors will internally use the Kafka Consumer API.

## How Kafka Connect works?

Kafka Developers made a smart decision and created a brand new framework for implementing Kafka connector. They named it Kafka connect framework and open-sourced it. The Kafka connect framework allows you to write connectors.

These connectors are implemented in two flavors:    
1. **Source Connector**
    - SourceConnector
    - SourceTask
2. **Sink Connector**
    - SinkConnector
    - SinkTask

The Kafka connect framework takes care of all the heavy lifting, scalability, fault tolerance, error handling, and bunch of other things. As a connector developer, all you need to do is to `implement two Java classes`. The first one is `SourceConnector` or `SinkConnector` class. And the second one is the `SourceTask` or the `SinkTask`.

Once your Connector is developed and tested, you can package it as a Uber Jar or as a Zip archive. Share it with others, and they should be able to use it.

So assume you want to bring some data from an RDBMS to a Kafka Cluster. All you need to do is to `take an appropriate source connector`, for example a JDBC source connector. Then `you install it` in your Kafka connect, `configure it` and `run it`. That's all. The JDBC Connector will take care of the rest.

 Similarly, when you wanted to move data from your Kafka Cluster to your Snowflake database, `get this sink connector`, `install it` in your Kafka connect,` configure it`, `run it`, and boom. You are done.

 ## Can we scale Kafka Connect?

 The Kafka Connect itself is a Cluster. Each individual unit in the Connect Cluster is called a **Connect Worker**. You can play with the number of tasks and scale the Cluster capacity by adding more workers. 

 You can have one Kafka connect Cluster and run as many connectors as you want. We can have one source connector and one Sink connector running in the same Kafka connect Cluster.

 ## Connect Transformation

 Kafka connect also allowed some fundamental **Single Message Transformations** (SMTs). This means you can apply some transformations or changes to each message on the fly. And this is allowed with both source and Sink connectors.

Here is a list of some common SMTs:

- Add a new field in your record using a static radar metadata. 
- Filter or rename fields. 
- Mask some fields with a Null Value.
- Change the record Key. 
- Route the record to a different Kafka Topic

You can chain multiple SMTs and play with it to restructure your records and route them to a different topic. However, these SMTs are not good enough to perform some real life data validations and transformations.

## Kafka Connect Architecture

To learn the Kafka connect architecture, you have to understand three things. 
- Worker
- Connector 
- Task

### Worker
You already learned that Kafka Connect is a cluster, and it runs `one or more workers`. So let's assume you started a Kafka Connect Cluster with three workers. These workers are fault tolerant, and they use the Group ID to form a Cluster. This Group ID mechanism is the same as Kafka Consumer Groups. So, all you need to do is to start workers with the same group id, and they will join hands to form a Kafka Connect Cluster. These workers are the main workhorse of the Kafka Connect.

That means, `they work like a container process, and they will be responsible for starting and running Connector and the Task`. These workers are fault-tolerant and self-managed. 

If a worker processes stops or crashes other workers in the Connect Cluster will recognize that and reassign the connectors and tasks that ran on that worker to the remaining workers. If a new worker joins a connect Cluster, other workers will notice that and assign connectors or tasks to it and make sure the load is balanced.

 So, in a nutshell, these workers will give you reliability, higher availability, scalability and load balancing.

 ### Connector 

 Now all you need to do is to copy the data. Let's assume I wanted to copy data from a relational database. So, I'll `download the JDBC Source Connector`, `install it within the Cluster`. Well, the installation is to make sure the JAR files and all its dependencies are made available to these workers. There is nothing anything special about the installation.

 The next thing is to `configure the connector`. So the configuration means providing some necessary information. For example, database connection details, a list of tables to copy, frequency to pull the source for the new data, the maximum number of tasks, and many other things depending upon your Connector and the requirement. All this configuration goes into a file, and you will start the connector using some command line tool.
 > Kafka Connect also offers you REST APIs so you can even begin the connector using the REST API instead of the command line tool. 

At this stage, one of the workers will start your Connector process.*Because workers are like a container* they start and run other processes.

Now the connector process is mainly responsible for two things. The first thing is to determine the degree of **parallelism** - how many parallel tasks can I start to copy the data from this source.

So the first thing is to decide `how to split the data copying work`. For example, let's assume I wanted to ingest data from five tables. So, I listed five tables in the configuration side and it started the JDBC connector.

```
DB Connection details = XXXX
Source Tables LIST = T1, T2, T3, T4, T5
```

Now it is quite natural for the connector to detect the splitting mechanism: **One table per task**. So the maximum number of parallelism is five in this case. 

```
DB Connection details = XXXX
Source Tables LIST = T1, T2, T3, T4, T5
Polling Frequency = 5 min
Max parallelism = 5
```

However, the splitting logic for different source systems is written in the connector code, and you give configuration options accordingly. So you must know your Connector and configure it accordingly.

### Task

So, the connector knows that it can start five parallel tasks and assign one table to each task of copying data from the source system. `Remember, the connector is not going to copy the data.` It is only responsible for defining and creating a task list. Each task will be configured to read data from an assigned list of tables.

In our example, it is just one table. The Connector will also include some additional configurations such as database connection details and other things to make sure that the task can operate as an independent process.

```
Task Name = Task1   Tasl Configs = XXXX
Task Name = Task2   Tasl Configs = XXXX
Task Name = Task3   Tasl Configs = XXXX
Task Name = Task4   Tasl Configs = XXXX
Task Name = Task5   Tasl Configs = XXXX
```

Finally, the list of tasks will be given to these workers, and they will start the task. So your task is distributed across the workers for balancing the Cluster load. Now the task is responsible for connecting to the source system, polling the data at a regular interval, collecting the records, and handing over it to the worker. `They do not send the record to the Kafka Cluster`. That task is only responsible for interacting with the external system.

`This source task will handover the data to the worker, and the worker is responsible for sending it to the Kafka`.

In the case of the Sink task, they get the Kafka record from the worker, and the task is only responsible for inserting the record into the target system.

### Why is it designed like this?

Well, that's the reusable design.

Reading and writing data to a Kafka Cluster is a standard activity. So it is taken care of by the framework.

We have two things that are changing for different source and target systems:

- `How to split the input for parallel processing`. This is taken care of by the **Connector class**. 

- `How to interact with the external system`. This is also taken care of by the **Task class**. 

And these are the things that a connector developer needs to take care of. 

Most of the other stuff like interacting with Kafka, handling configurations,
errors, monitoring connectors, and tasks, scaling up and down, and handling failures are standard things and are taken care of by the Kafka Connect Framework.

# Summary
Kafka Connect is a component of Kafka for connecting and moving data between Kafka and external systems. We have two types of Kafka Connectors. Source Connector and Sink connector. And together, they support a bunch of systems and offer you an out of the box data integration capability without writing a single line of code.