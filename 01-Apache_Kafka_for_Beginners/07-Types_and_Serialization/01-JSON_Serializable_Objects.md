# JSON Serializable Objects

We will be creating some schema definitions  and generate POJOs out of those schemas. Let me define a problem statement that we will be solving in this lecture. We want to be able to model an Invoice event.

Our invoice is a complex Java Object that contains simple fields such as Invoice numbers, Created Time, Customer ID, Total Amount, etc. These fields could be of String, Long, Number, and Integer types.

| ClassName       | Invoice           |
|-----------------|-------------------|
| Field Name      | Type              |
| InvoiceNumber   | String            |
| CreateTime      | Long              |
| CustomerCardNo  | String            |
| TotalAmount     | Number            |
| NumberOfItems   | Integer           |
| PaymentMethod   | String            |
| TaxableAmount   | Number            |
| CGST            | Number            |
| SGST            | Number            |
| CESS            | Number            |
| InvoiceLineItem | Array of LineItem |



Other than these simple fields, we also want to have an array of Invoice Line Items. An invoice Line item is another complex object which is defined separately and contains item details such as item description, price, and quantity.

| ClassName       | LineItem |
|-----------------|----------|
| Field Name      | Type     |
| ItemCode        | String   |
| ItemDescription | String   |
| ItemPrice       | Number   |
| ItemQty         | Integer  |
| TotalValue      | Number   |

We also want to define a separate object for a delivery address containing fields like address line, state, pin code, etc.

| ClassName     | DeliveryAddress |
|---------------|-----------------|
| Field Name    | Type            |
| AddressLine   | String          |
| City          | String          |
| State         | String          |
| PinCode       | String          |
| ContactNumber | String          |

Finally, we want to combine all of these objects to create POS invoice.

| ClassName       | PosInvoice      |
|-----------------|-----------------|
| Field Name      | Type            |
| StoreID         | String          |
| CashierID       | String          |
| CustomerType    | String          |
| DeliveryType    | String          |
| DeliveryAddress | DeliveryAddress |


The POS Invoice inherits the Invoice object.So, we automatically get all fields and array of line items from the Invoice Object. However, we also want to add some additional fields such as store id, cashier id, delivery type, and delivery address.

We will be transmitting POSInvoice to Kafka broker. However, we have modeled various objects separately and also included a scenario for extending a class.
The example represents a typical Java Object-Oriented modeling approach.

## How do we model our events as we model the usual java classes?

We want to define these objects using a simple schema definition language and automatically generate serializable classes. we are going to use an open-source project named [jsonschema2pojo](https://github.com/joelittlejohn/jsonschema2pojo.
You might find other better alternatives or build your own custom tool for the purpose. However, JsonSchema2Pojo is a reasonable choice for being able to produce Java as well as Scala code.

Let's create a new Maven project and define schemas for our object.

- Create a folder `shcema` under project resources. This is the place where we will keep all the schema definitions. We will create the first schema definition, and  will use the JSON syntax. The syntax is straightforward:

```json
{
  "type" : "object",
  "javaType" : "org.example.types.LineItem",
  "properties" : {
    "ItemCode" : {"type" :  "string"},
    "ItemDescription" : {"type" :  "string"},
    "ItemPrice" : {"type" :  "number"},
    "ItemQty" : {"type" :  "integer"},
    "TotalValue" : {"type" :  "number"}
  }
}
```

Define the type as an object. Then we tell the fully qualified name of the target java type. These two things are required for almost every schema definition, and they specify the target Java Object and java package details. Every field in the target class should be defined inside the properties.

Let's create another one for DeliveryAddress.

```json
{
  "type" : "object",
  "javaType" : "org.example.types.DeliveryAddress",
  "properties" : {
    "AddressLine" : {"type" :  "string"},
    "City" : {"type" :  "string"},
    "State" : {"type" :  "string"},
    "PinCode" : {"type" :  "string"},
    "ContactNumber" : {"type" :  "string"}
  }
}
```

The Invoice.

```json
{
  "type": "object",
  "javaType": "org.example.types.Invoice",
  "properties": {
    "InvoiceNumber": {"type": "string"},
    "CreatedTime": {"type": "object", "javaType": "java.lang.Long"},
    "CustomerCardNo": {"type": "string"},
    "TotalAmount": {"type": "number"},
    "NumberOfItem": {"type": "integer"},
    "PaymentMethod": {"type": "string"},
    "TaxableAmount": {"type": "number"},
    "CGST": {"type": "number"},
    "SGCT": {"type": "number"},
    "CESS": {"type": "number"},
    "InvoiceLineItems": {"type": "array" ,
      "items": {
        "type": "object",
        "$ref": "LineItem.json"
      }
    }
  }
}
```
We create Type and Java Type. Then we define properties. Wewanted to define the create time as a Long value. The documentation doesn't have anything for creating a Long. However, it allows us to set a Java Class Name. All other fields are similar to the earlier schema. The last one is Invoice Line Items. And this one should be an array. We also need to define the items of the array. The type of each element of the array is an object and for the detailed definition of the object, you can refer to LineItems.json file.

The PosInvoice. 

```json
{
  "type": "object",
  "javaType": "org.example.types.PosInvoice",
  "extends": {"$ref": "Invoice.json"},
  "properties": {
    "StoreID": {"type": "string"},
    "PosID": {"type": "string"},
    "CustomerType": {"type": "string"},
    "DeliveryType": {"type": "string"},
    "DeliveryAddress": {
      "type": "object",
      "$ref": "DeliveryAddress.json"
    }
  }
}
```
We wanted the POSInvoice to extend the invoice. We do not have a requirement to implement a Java Interface in this example. However, the documentation says, you can do that also. Rest of the fields are straightforward.
The delivery address is an object which is not yet generated, so we add the file reference.

## How do we generate class definitions?

The jsonschema2pojo comes as a maven plugin. All you need is to add some details in your pom file.

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

            <!-- Json Schema to POJO plugin-->
            <plugin>
                <!-- plugin's maven coordinates -->
                <groupId>org.jsonschema2pojo</groupId>
                <artifactId>jsonschema2pojo-maven-plugin</artifactId>
                <version>0.5.1</version>
                <!-- execution details -->
                <executions>
                    <execution>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                        <configuration>
                            <!-- source directory for schema definition files -->
                            <sourceDirectory>${project.basedir}/src/main/resources/schema/</sourceDirectory>
                            <!-- output directory where all the generated packages and classes should land -->
                            <outputDirectory>${project.basedir}/src/main/java/</outputDirectory>
                            <includeAdditionalProperties>false</includeAdditionalProperties>
                            <includeHashcodeAndEquals>false</includeHashcodeAndEquals>
                            <generateBuilders>true</generateBuilders>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <!--Apache commons-->
        <dependency>
            <groupId>commons-lang</groupId>
            <artifactId>commons-lang</artifactId>
            <version>2.6</version>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version></version>
        </dependency>

    </dependencies>

```

Ok, so this is what it's need. There are the plugin's maven coordinates.
s. In the configuration, we tell the source directory for the schema definition files. Then we tell the output directory where all the generated packages and classes would land. We will also need the Jackson data bind. We will also need Apache Commons. We don`t need it in fact, but the JSON schema to POJO requires that.

We can jump to maven lifecycle and compile the project.

Wait for a minute and done.

All of your classes are generated, and the class definition comes with Jackson annotations.

```java

package org.example.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "AddressLine",
    "City",
    "State",
    "PinCode",
    "ContactNumber"
})
public class DeliveryAddress {

    @JsonProperty("AddressLine")
    private String addressLine;
    @JsonProperty("City")
    private String city;
    @JsonProperty("State")
    private String state;
    @JsonProperty("PinCode")
    private String pinCode;
    @JsonProperty("ContactNumber")
    private String contactNumber;

    @JsonProperty("AddressLine")
    public String getAddressLine() {
        return addressLine;
    }

    @JsonProperty("AddressLine")
    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public DeliveryAddress withAddressLine(String addressLine) {
        this.addressLine = addressLine;
        return this;
    }

    @JsonProperty("City")
    public String getCity() {
        return city;
    }

    @JsonProperty("City")
    public void setCity(String city) {
        this.city = city;
    }

    public DeliveryAddress withCity(String city) {
        this.city = city;
        return this;
    }

    @JsonProperty("State")
    public String getState() {
        return state;
    }

    @JsonProperty("State")
    public void setState(String state) {
        this.state = state;
    }

    public DeliveryAddress withState(String state) {
        this.state = state;
        return this;
    }

    @JsonProperty("PinCode")
    public String getPinCode() {
        return pinCode;
    }

    @JsonProperty("PinCode")
    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public DeliveryAddress withPinCode(String pinCode) {
        this.pinCode = pinCode;
        return this;
    }

    @JsonProperty("ContactNumber")
    public String getContactNumber() {
        return contactNumber;
    }

    @JsonProperty("ContactNumber")
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public DeliveryAddress withContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("addressLine", addressLine).append("city", city).append("state", state).append("pinCode", pinCode).append("contactNumber", contactNumber).toString();
    }

}

```
These annotations make these classes serializable using the JSON serializer and deserializer that we have included in the examples of this course.