# JDBC Sink Connector Distributed Mode
- Goal:
    - Start an PostgresSQL instance using Docker
    - Run in distributed mode with multiple tasks
- Learning:
    - Learn about the JDBC Sink Connector. 

Create a new JDBC Sink connector and copy the properties. 
PostgresSQL properties 
```properties
# Basic configuration for our connector
name=sink-postgres-twitter-distributed
connector.class=io.confluent.connect.jdbc.JdbcSinkConnector
# We can have parallelism here so we have two tasks!
tasks.max=1
topics=demo-3-twitter
# the input topic has a schema, so we enable schemas conversion here too
key.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schemas.enable=true
value.converter=org.apache.kafka.connect.json.JsonConverter
value.converter.schemas.enable=true
# JDBCSink connector specific configuration
# http://docs.confluent.io/3.2.0/connect/connect-jdbc/docs/sink_config_options.html
connection.url=jdbc:postgresql://postgres:5432/postgres
connection.user=postgres
connection.password=postgres
insert.mode=upsert
# we want the primary key to be offset + partition
pk.mode=kafka
# default value but I want to highlight it:
pk.fields=__connect_topic,__connect_partition,__connect_offset
fields.whitelist=id,created_at,text,lang,is_retweet
auto.create=true
auto.evolve=true
```

Now you will see 2 connectors reading from the same topic. 

## Test
let's start a command line
```
docker run -it --rm --net=host jbergknoff/postgresql-client postgresql://postgres:postgres@127.0.0.1:5432/postgres 
```
Query the table
```
postgres=# \dt
             List of relations
 Schema |      Name      | Type  |  Owner   
--------+----------------+-------+----------
 public | demo-3-twitter | table | postgres 
(1 row)

postgres=# select count(*) from "demo-3-twitter";
 count     
-------    
   588     
(1 row)    

postgres=# select id,text from "demo-3-twitter" limit 3;
         id          |                                                                     text
---------------------+----------------------------------------------------------------------------------------------------------------------------------------------
 1503841121788702728 | RT @JobPreference: #Hiring?                                                                                                                 +
                     | Sign up now https://t.co/o7lVlsCHXv                                                                                                         +
                     | FREE. NO MIDDLEMAN                                                                                                                          +
                     | #ArtificialIntelligence #MachineLearning #Python #DataSc…
 1503841135042732035 | @58bugeye @GovRonDeSantis Your arguing a problem you made up, nowhere in this bill does it say what you are implyin… https://t.co/NqLgmdC62U
 1503841186531917824 | RT @JobPreference: #Hiring?                                                                                                                 +
                     | Sign up now https://t.co/o7lVlsl75X                                                                                                         +
                     | FREE. NO MIDDLEMAN                                                                                                                          +
                     | #ArtificialIntelligence #MachineLearning #Python #DataSc…
(3 rows)
```