# Kafka Connect REST API
- All the actions performed by Landoop Kafka Connect Ul are actually
triggering REST API calls to Kafka Connect.

- Let's learn about those (http://docs.confluent.io/3.2.0/connect/managing.html#common-rest-examples)

 1. Get Worker information
 2. List Connectors available on a Worker
 3. Ask abou Active Connectors
 4. Get information about a Connector Tasks and Config
5. Get Connector Status
6. Pause / Resume a Connector
7. Delete our Connector
8. Create a new Connector
9. Update Connector configuration
10. Get Connector Configuration

let's start a command line to all have linux commands
```
docker run --rm -it --net=host landoop/fast-data-dev:cp3.3.0 bash
```
Install jq to pretty print json
```
apk update && apk add jq
```


## 1) Get Worker information
```
curl -s 127.0.0.1:8083/ | jq
```
## 2) List Connectors available on a Worker
```
curl -s 127.0.0.1:8083/connector-plugins | jq
```
## 3) Ask about Active Connectors
```
curl -s 127.0.0.1:8083/connectors | jq
```
## 4) Get information about a Connector Tasks and Config
```
curl -s 127.0.0.1:8083/connectors/source-twitter-distributed/tasks | jq
```
## 5) Get Connector Status
```
curl -s 127.0.0.1:8083/connectors/file-stream-demo-distributed/status | jq
```
## 6) Pause / Resume a Connector (no response if the call is succesful)
```
curl -s -X PUT 127.0.0.1:8083/connectors/file-stream-demo-distributed/pause
curl -s -X PUT 127.0.0.1:8083/connectors/file-stream-demo-distributed/resume
```
## 7) Get Connector Configuration
```
curl -s 127.0.0.1:8083/connectors/file-stream-demo-distributed | jq
```
## 8) Delete our Connector
```
curl -s -X DELETE 127.0.0.1:8083/connectors/file-stream-demo-distributed
```
## 9) Create a new Connector
```
curl -s -X POST -H "Content-Type: application/json" --data '{"name": "file-stream-demo-distributed", "config":{"connector.class":"org.apache.kafka.connect.file.FileStreamSourceConnector","key.converter.schemas.enable":"true","file":"demo-file.txt","tasks.max":"1","value.converter.schemas.enable":"true","name":"file-stream-demo-distributed","topic":"demo-2-distributed","value.converter":"org.apache.kafka.connect.json.JsonConverter","key.converter":"org.apache.kafka.connect.json.JsonConverter"}}' http://127.0.0.1:8083/connectors | jq
```
## 10) Update Connector configuration
```
curl -s -X PUT -H "Content-Type: application/json" --data '{"connector.class":"org.apache.kafka.connect.file.FileStreamSourceConnector","key.converter.schemas.enable":"true","file":"demo-file.txt","tasks.max":"2","value.converter.schemas.enable":"true","name":"file-stream-demo-distributed","topic":"demo-2-distributed","value.converter":"org.apache.kafka.connect.json.JsonConverter","key.converter":"org.apache.kafka.connect.json.JsonConverter"}' 127.0.0.1:8083/connectors/file-stream-demo-distributed/config | jq
```