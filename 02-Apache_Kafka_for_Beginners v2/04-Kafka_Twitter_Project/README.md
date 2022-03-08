# Set up 
1. Create a Twitter account if don't have one already. 
2. Get a developer account from [Developer Twitter](https://developer.twitter.com/)
3. Create the Project and App.
4. Go to this [Git Repo](https://github.com/twitter/hbc) and add the following dependency to the project:
```xml
  <dependency>
      <groupId>com.twitter</groupId>
      <artifactId>hbc-core</artifactId> <!-- or hbc-twitter4j -->
      <version>2.2.0</version> <!-- or whatever the latest version is -->
    </dependency>
```