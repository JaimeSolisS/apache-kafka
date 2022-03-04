# Set Environment 
## Multi-node Cluster
Inside config dir, make 2 more copies of server.properties file and rename them as . server-0, server-1 and server-2. Change their broker.id property to make them unique. 

server-0.properties
```properties
############################# Server Basics #############################

# The id of the broker. This must be set to a unique integer for each broker.
broker.id=0
```

server-1.properties
```properties
############################# Server Basics #############################

# The id of the broker. This must be set to a unique integer for each broker.
broker.id=1
```

server-2.properties
```properties
############################# Server Basics #############################

# The id of the broker. This must be set to a unique integer for each broker.
broker.id=2
```

The next most critical property is the listener port. The port config is commented, and hence the broker gets a default port. This is the port number at which the producers are going to send data to the Kafka broker.

For the first broker, we will leave the default value 9092. But for the second broker, we will replace it for 9093 similarly for the third broker we are changing it to 9094.

server-0.properties
```properties
############################# Socket Server Settings #############################

# The address the socket server listens on. It will get the value returned from 
# java.net.InetAddress.getCanonicalHostName() if not configured.
#   FORMAT:
#     listeners = listener_name://host_name:port
#   EXAMPLE:
#     listeners = PLAINTEXT://your.host.name:9092
listeners=PLAINTEXT://:9092
```
server-0.properties
```properties
############################# Socket Server Settings #############################

# The address the socket server listens on. It will get the value returned from 
# java.net.InetAddress.getCanonicalHostName() if not configured.
#   FORMAT:
#     listeners = listener_name://host_name:port
#   EXAMPLE:
#     listeners = PLAINTEXT://your.host.name:9092
listeners=PLAINTEXT://:9093
```
server-0.properties
```properties
############################# Socket Server Settings #############################

# The address the socket server listens on. It will get the value returned from 
# java.net.InetAddress.getCanonicalHostName() if not configured.
#   FORMAT:
#     listeners = listener_name://host_name:port
#   EXAMPLE:
#     listeners = PLAINTEXT://your.host.name:9092
listeners=PLAINTEXT://:9094
```
The third configuration is the Kafka log directory location. This is the directory location where Kafka is going to store the partition data. When you are running multiple brokers on the same machine, you should also assign a different directory to each broker. Change the data log directory to the current directory, and every time you start the Kafka cluster from your IDE, it will create new logs in the current directory. It works like running separate Kafka cluster for each example.

server-0.properties
```properties
############################# Log Basics #############################

# A comma separated list of directories under which to store log files
log.dirs=../tmp/kafka-logs-0
```
server-1.properties
```properties
############################# Log Basics #############################

# A comma separated list of directories under which to store log files
log.dirs=../tmp/kafka-logs-1
```
server-2.properties
```properties
############################# Log Basics #############################

# A comma separated list of directories under which to store log files
log.dirs=../tmp/kafka-logs-2
```

Change data dir in zookeeper.properties file.
```properties
dataDir=../tmp/zookeeper
```
## Set up an environment variable.
To avoid typing absolute part all the time, set the KAFKA environment variable. Here is the command: setx KAFKA your/path/to/kafka/dir This command works only on Windows.