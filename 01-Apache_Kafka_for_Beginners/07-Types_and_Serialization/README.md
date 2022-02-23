# Types and Serialization 

We've learned how to create Kafka producers and send data to the Kafka cluster. However, we have been sending simple text messages. But the real life records are not plain text or string messages. They are complex Java Objects.

Kafka programming is mainly dealing with data records in a variety of formats, and that brings two critical questions. 

- How to create Java types (POJOs),
- How to serialize your Java types (POJOs)

A simple example can manage with 4-5 types. However, in a real life scenario, a complex data processing requirement can quickly scale up to hundreds of unique record formats. Creating POJO for the message type is a tedious mechanical activity.

## Can we automate that?

1. Use a schema definition language - We want to be able to define a message schema using some simple Schema Definition language.

2. Auto-generate Java Class definition - Then we want our IDE or the build tool to generate Java class definition from the schema definition automatically.

There are many ways and several tools to help you achieve this. However, there are two alternatives that are suggested for your Kafka applications.

1. JSON schema to POJO
2. Avro Schema to POJO. 

We have enough open source support for generating POJO for both these options, and we'll cover one for each of these formats. JSON and Avro. However, generating POJOs are just the first half of the problem.

The second part of the requirement is to serialize and deserialized them. Every Kafka application would use a bunch of Java types, and you must provide a serializer and a deserializer for all of them. Creating a serializer of each kind is a big headache. 

## Can we develop reusable serializers and deserializers and apply the same to all of the Java types?

There are many objects serialization formats, but for your Kafka application, two alternatives are suggested, which are the most commonly used formats. JSON Serialization and Avro Serialization. 

In this section, we will learn the following things:

1. JSON Schema to POJO

We will learn to define this schema of your events using JSON and how you can auto-generate a serializable POJO definition from the schema definition.

2. Avro Schema to POJO

Then, we will also learn to define the schema of your events using Avro Schema Definition language and how you can auto-generate a serializable POJO definition from the Avro Schema Definition.

Finally, we'll serialize these objects using JSON as well as Avro serialization.