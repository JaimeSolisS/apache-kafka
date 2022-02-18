# Development Environment

We'll use IntelliJ IDEA as development IDE and set up the environment step by step. 

## Configure IDE for Kafka Development

The earlier configuration is going to create log files in the /tmp/ directory
> ### Kafka Log File Configuration
>/tmp/kafka-logs-0
>/tmp/kafka-logs-1
>/tmp/kafka-logs-2

This configuration is perfectly fine. However, we will be running multiple examples and using the same Kafka cluster installation for all the examples. Sometimes, this approach is not convenient for local development. So we want to use a separate data directory for each example.

Change the data log directory to the current directory, and every time you start the Kafka cluster from your IDE, it will create new logs in the current directory. It works like running separate Kafka cluster for each example.

Change data dir in zookeeper.properties file.
```
...
dataDir=../tmp/zookeeper
...
```
Now do the same with server.properties file. 

```
############################# Log Basics #############################

# A comma separated list of directories under which to store log files
log.dirs=../tmp/kafka-logs-0
```
Repeat it for all the three configuration files.

### Set up an environment variable.

 To avoid typing absolute part all the time,  set the KAFKA_HOME environment variable. Here is the command: `setx KAFKA_HOME your/path/to/dir`
 **This command works only on Windows**.

# Uncompress project 
You should see a directory struccture similar to this:
 ```
01-storage-demo/
├─ .idea/
├─ scripts/
├─ src/
├─ 01-storage-demo.iml
├─ pom.xml
 ```
 So now, we are ready to start the IntelliJ IDEA and open one example project.

All the examples and this course will include some scripts to start Kafka services, create topic and other things which are needed to run the example.The current example has got these many scripts. 
```

01-storage-demo/
├─ .idea/
├─ scripts/
│  ├─ 0-kafka-server-start.cmd
│  ├─ 1-kafka-server-start.cmd
│  ├─ 2-kafka-server-start.cmd
│  ├─ describe-topic.cmd
│  ├─ topic-create.cmd
│  ├─ zookeeper-start.cmd
├─ src/
├─ 01-storage-demo.iml
├─ pom.xml
```
All these scripts are windows batch files.

zookeper-start.cmd 
```
%KAFKA_HOME%\bin\windows\zookeeper-server-start.bat %KAFKA_HOME%\etc\kafka\zookeeper.properties
```

So this script is to start the zookeeper. So we are using KAFKA_HOME environment variable and then navigating to bin\windows and starting the batch file to run the zookeeper. Similarly, we are specifying the zookeeper properties file location using the environment variable. **These scripts will work on Windows without any change**. For Linux or Mac, the script should be slightly modified. 

After starting zookeeper and all 3 brokers we will see a tmp dir that has been created for the kafka and zookeper data in the current directory.

```
01-storage-demo/
├─ .idea/
├─ scripts/
│  ├─ 0-kafka-server-start.cmd
│  ├─ 1-kafka-server-start.cmd
│  ├─ 2-kafka-server-start.cmd
│  ├─ describe-topic.cmd
│  ├─ topic-create.cmd
│  ├─ zookeeper-start.cmd
├─ src/
├─ target/
├─ tmp/
│  ├─ kafka-logs-0/
│  ├─ kafka-logs-1/
│  ├─ kafka-logs-2/
├─ 01-storage-demo.iml
├─ pom.xml
```

Now we can run the example, send some data. Process it and play it the way we want. All the data for this project remains in the current directory of this project. When we start another project, the log files for the other project remains isolated in the current directory of the other project.

When we are done with this project or if we want to clean up and restart things fresh, I'll stop all services and empty this tmp directory. 

This simple approach makes life quite comfortable during the development phase.