# Sending AVRO Messages 

Last was sending realistic invoices to Kafka, but those messages were serialized using JSON serializer. We will take the same example, basically copy the code, and make some necessary changes, so it starts sending AVRO invoices.

Let's create a solution. First thing first, AVRO serializer is offered by Confluent Platform, and we are going to develop and execute this example on Confluent community edition. We are going to create a brand new maven project from scratch.

Let's create an empty maven project. The first thing is to set up the necessary maven dependencies.

Let's do it.

We will be using JDK 1.8 like all other projects. So let's define a property for the same. We will be using Confluent 5.3.0. So, one property for the confluent version.,Instead of using Apache Kafka, we will be using a Kafka version which comes with Confluent 5.3.0. Using Confluent Kafka version is necessary to avoid version conflicts because we are going to execute our program on Confluent platform and hence we need the same version. And this is how Confluent defines Kafka version in their platform.

```xml
    <properties>
        <java.version>1.8</java.version>
        <confluent.version>5.3.0</confluent.version>
        <kafka.version>5.3.0-ccs</kafka.version>
    </properties>
```

Confluent artifacts are not hosted in standard Maven repository. So, we need to add a custom repository here. So, confluent packages will come from this repository.

```xml
    <repositories>
        <repository>
            <id>confluent</id>
            <url>https://packages.confluent.io/maven/</url>
        </repository>
    </repositories>
```

Now, we are ready to define the dependencies. We need Kafka Avro serializer offered by Confluent. When we serialized our messages using JSON, we used a JSON serializer that we created. But now, we want to serialize our messages using AVRO, and we will be using AVRO serializer that Confluent has created. Next one is Kafka Client. We are going to create a producer, so we need the Kafka Client libraries. The last dependency is for log4j.

```xml
    <dependencies>
        <!-- Confluent Kafka Avro Serializer-->
        <dependency>
            <groupId>io.confluent</groupId>
            <artifactId>kafka-avro-serializer</artifactId>
            <version>${confluent.version}</version>
        </dependency>
        <!-- Apache Kafka Clients-->
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
            <version>${kafka.version}</version>
        </dependency>
        <!-- Apache Log4J2 binding for SLF4J -->
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-slf4j-impl</artifactId>
            <version>2.11.0</version>
        </dependency>
    </dependencies>
```
We also need the AVRO Maven plugin. 
```xml
<build>
        <plugins>
            <!-- Maven Compiler Plugin-->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.0</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                </configuration>
            </plugin>

            <!-- Maven Avro plugin for generating pojo-->
            <plugin>
                <groupId>org.apache.avro</groupId>
                <artifactId>avro-maven-plugin</artifactId>
                <version>1.8.1</version>
                <executions>
                    <execution>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>schema</goal>
                        </goals>
                        <configuration>
                            <sourceDirectory>${project.basedir}/src/main/resources/schema/</sourceDirectory>
                            <outputDirectory>${project.basedir}/src/main/java/</outputDirectory>
                            <imports>
                                <import>${project.basedir}/src/main/resources/schema/LineItem.avsc</import>
                                <import>${project.basedir}/src/main/resources/schema/DeliveryAddress.avsc</import>
                            </imports>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```
Copy the schema definitions from previous example that generated Java Classes from AVRO schema and paste them under resources dir. We also need a log4j configuration file. So lets copy-paste here from another earlier project.

After getting the Schema definition and Maven dependencies, we are ready to compile this project and get our Java Classes.

We reached the state of the earlier project. If you are worried about some warnings, place a line in your pom file properties. Recompile, and it should go away.

```xml
    <properties>
        <java.version>1.8</java.version>
        <confluent.version>5.3.0</confluent.version>
        <kafka.version>5.3.0-ccs</kafka.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
```

## Recap

- We created an empty project.
- Defined some dependencies to work with the confluent platform.
- Then we copied the Avro schema definition from our earlier project.
- In summary, we just replication our Avro to POJO project with some additional dependencies.

So far, this project is essentially the same as the Avro-to-POJO example. Now we want to take it forward and make a pos simulator. But we have already done that for JSON. This project is also going to do the same thing except for one fundamental difference. Instead of sending JSON, it should send AVRO.

So, open the old POS simulator and copy most of the code from that project and make only necessary changes. We need the datagenerator and sample data directories. Copy them to current project.  

Invoice generator class shows an error, A string cannot be applied to Char Sequence. Well, the Avro Maven plugin uses char sequence instead of String. We can fix this problem so change this this line to
```java
if ("HOME-DELIVERY".equalsIgnoreCase(invoice.getDeliveryType().toString())) {
```

We do not need  Json Serializer because we are going to use AVRO serializer.

Now, we need these three classes AppConfigs, PosSimulator, RunnableProducer. Let's  copy them also.

Ok, Runnable Producer is showing an error. Cannot infer arguments. It cannot understand types because getStoreID is returning a char sequence. Let me convert it to a String.
```java
producer.send(new ProducerRecord<>(topicName, posInvoice.getStoreID().toString(), posInvoice));
```


Now comes the POS simulator. This one is the main class where we need some real changes. At a high level, everything till now is almost the same as it was for JSON based producer. Now we are going to make some changes that are the fundamental changes for making it work with the AVRO.

- JSON Serializer. We must change this one to KafkaAvroSerializer.
```java
properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
```

Confluent Avro Serializer does not work without the Confluent Schema registry. So, even if you don't need it, you must add one configuration for the schema registry. Here is the line that you need to add.

```java
properties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
```
You can also move this constant to App Configs.
```java
 properties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, AppConfigs.schemaRegistryServers);
```

##  So, how is the JSON project different from this AVRO project.

In summary, All you need is four main things.

- Schema definition using AVRO schema and Avro compatible types.

- KafkaAvroSerializer

- Schema registry configuration.

- Confluent specific dependencies.

That's all.

Do you want to test it?

We need scripts. Lets copy all the scripts from the older project. Well, the scripts are mostly the same, but we want them to execute from the confluent home directory. So, lets prefix the confluent home directory location in all the scripts. We also want to change the configuration file location. 

You also need to start the schema registry. So let's add one script for the same.
```cmd
%KAFKA_HOME%\bin\windows\schema-registry-start.bat %KAFKA_HOME%\etc\schema-registry\schema-registry.properties
```

Now we can start the services, create the topic, and run our simulator.

If you start a consumer, you can see some content. But Avro is a binary format. So we can't understand it.
