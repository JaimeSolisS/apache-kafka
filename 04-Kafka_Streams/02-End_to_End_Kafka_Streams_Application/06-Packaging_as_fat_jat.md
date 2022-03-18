# Packaging the application as a fat jar

- In order to deploy the application to other machines, we often need to compile it as a `.jar` (Java ARchive).
- Default compilation in java only includes the code you write in the .jar
  file, without the dependencies
- Maven has a plugin to allow us to package all our code + the
  dependencies into one jar, simply called a `fat jar`

## Package application as a fat jar

Add the following pluging to pom file
```xml
 <!--package as one fat jar-->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                    <archive>
                        <!--Specify Main Class full path-->
                        <manifest>
                            <addClasspath>true</addClasspath>
                            <mainClass>org.jsolis.kafka.streams.StreamsStarterApp</mainClass>
                        </manifest>
                    </archive>
                </configuration>
                <executions>
                    <execution>
                        <id>assemble-all</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
``` 

Create the package with Intelliji or in terminal
```cmd
mvn clean package
```

## Run application from the fat jar

```
java -jar target/WordCount-1.0-jar-with-dependencies.jar
```