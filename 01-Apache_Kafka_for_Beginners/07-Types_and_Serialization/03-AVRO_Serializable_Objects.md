# AVRO Serializable Objects

Avro is pretty much a standard and very well accepted by the community for Bigdata projects. If you prefer to use Avro, you can quickly generate POJO from an Avro schema with the same kind of simplicity that we learned for JSON. So let's take the same set of schemas that we used for learning JSON part of it.

|ClassName: Invoice |LineItem|DeliveryAddress|PosInvoice|
|--|--|--|--|
|<table><tr><th>Field Name</th><th>Type</th></tr><tr><td>InvoiceNumber</td><td>String</td></tr><tr><td>CreatedTime</td><td>Long</td></tr><tr><td>CustomerCardNo</td><td>String</td></tr> <tr><td>TotalAmount</td><td>Number</td></tr><tr><td>NumberOfItems</td><td>Integer</td></tr><tr><td>PaymentMethod</td><td>String</td></tr><tr><td>TaxableAmount</td><td>Number</td></tr><tr><td>CGST</td><td>Number</td></tr><tr><td>SGST</td><td>Number</td></tr><tr><td>CESS</td><td>Number</td></tr><tr><td>InvoiceLineItem</td><td>Array of LineItem</td></tr></table>|<table><tr><th>Field Name</th><th>Type</th></tr><tr><td>ItemCode</th><td> String</td></tr><tr><td>ItemDescription</th><td>String</td></tr><tr><td>ItemPrice</td><td>Number</td></tr><tr><td>ItemQty</td><td>Integer</td></tr><tr><td>TotalValue</td><td>Number</td></tr></table>|<table><tr><th>Field Name</th><th>Type</th></tr><tr><td>AddressLine</td><td>String</td></tr><tr><td>City</td><td> String</td></tr><tr><td>State</td><td>String</td></tr><tr><td>PinCode</td><td>String</td></tr><tr><td>ContactNumber</td><td>String</td></tr></table>|<table><tr><th>Field Name</th><th>Type</th></tr><tr><td>StoreID</td><td>String</td></tr><tr><td>CashierID</td><td> String</td></tr><tr><td>CustomerType</td><td> String</td></tr><tr><td>DeliveryType</td><td> String</td></tr><tr><td>DeliveryAddress</td><td>DeliveryAddress</td></tr></table>|


However, there is a small limitation which requires us to remodel our schema. In the lecture on JSON schema to POJO, we created four schema definitions. First two are perfectly fine, and we can create them in AVRO as well. However, the other two schemas were to demonstrate inheritance implementation.

The `PosInvoice` schema extended the `Invoic`e schema for the JSON implementation. But that's a big problem for AVRO. As on date, Avro generated classes do not support inheritance. So, we cannot model java extends and implements construct for AVRO. What we can do is to combine the Invoice schema into PosInvoice schema. If we do that, we will eliminate the Invoice schema and will be left with only three schemas.

|ClassName: Invoice |LineItem|DeliveryAddress|PosInvoice|
|--|--|--|--|
|<table><tr><th>Field Name</th><th>Type</th></tr><tr><td>InvoiceNumber</td><td>String</td></tr><tr><td>CreatedTime</td><td>Long</td></tr><tr><td>StoreID</td><td>String</td></tr><tr><td>CashierID</td><td>String</td></tr><tr><td>CustomerCardNo</td><td>String</td></tr><tr><td>CustomerType</td><td>String</td></tr><tr><td>TotalAmount</td><td>Number</td></tr><tr><td>NumberOfItems</td><td>Integer</td></tr><tr><td>PaymentMethod</td><td>String</td></tr><tr><td>TaxableAmount</td><td>Number</td></tr><tr><td>CGST</td><td>Number</td></tr><tr><td>SGST</td><td>Number</td></tr><tr><td>CESS</td><td>Number</td></tr><tr><td>DeliveryType</td><td>String</td></tr><tr><td>DeliveryAddres</td><td>DeliveryAddres</td></tr><tr><td>InvoiceLineItem</td><td>Array of LineItem</td></tr></table>|<table><tr><th>Field Name</th><th>Type</th></tr><tr><td>ItemCode</th><td> String</td></tr><tr><td>ItemDescription</th><td>String</td></tr><tr><td>ItemPrice</td><td>Number</td></tr><tr><td>ItemQty</td><td>Integer</td></tr><tr><td>TotalValue</td><td>Number</td></tr></table>|<table><tr><th>Field Name</th><th>Type</th></tr><tr><td>AddressLine</td><td>String</td></tr><tr><td>City</td><td> String</td></tr><tr><td>State</td><td>String</td></tr><tr><td>PinCode</td><td>String</td></tr><tr><td>ContactNumber</td><td>String</td></tr></table>|


And that's going to be our baseline schemas for the current lecture. So, let's create a solution. We want to define these objects using a simple schema definition language and automatically generate serializable classes. So, let's create a new Maven project and define schemas.

Same as before, create a folder under project resources. This is the place where we will keep all the schema definitions. We will create the first schema definition, and will use Avro Schema definition syntax. The syntax is straightforward.

```json
{
  "namespace": "org.example.types",
  "type": "record",
  "name": "DeliveryAddress",
  "fields": [
    {"name": "AddressLine","type": ["null","string"]},
    {"name": "City","type": ["null","string"]},
    {"name": "State","type": ["null","string"]},
    {"name": "PinCode","type": ["null","string"]},
    {"name": "ContactNumber","type": ["null","string"]}
  ]
}
```

Define a namespace, and this would result in a java package. Then we tell the type of the element, and in most of the cases, it would be a record which will generate a java class definition. Then we provide the name of the class. These three things are required for almost every schema definition, and they specify the target Java Object and java package details. The next thing is to define the list of fields. Each field requires many details, and hence we enclose them in a pair of curly braces. Then we give a name of the field. The next attribute is obviously the type of the field. In our case, the type is going to be a String. You can optionally allow null for the field value. Enabling null is explicitly required for Avro schema. We define the rest of the fields in the same way.

Let's define the LineItem schema. This one is also straightforward. Give a namespace, type, and then class name. Finally, we add fields. All the fields are straightforward. We are defining a field name and then a type.

```json
{
  "namespace": "org.example.types",
  "type": "record",
  "name": "LineItem",
  "fields": [
    {"name": "ItemCode","type": ["null","string"]},
    {"name": "ItemDescription","type": ["null","string"]},
    {"name": "ItemPrice","type": ["null","double"]},
    {"name": "ItemQty","type": ["null","int"]},
    {"name": "TotalValue","type": ["null","double"]}
  ]
}
```
Let's create the last one. We start this one also in the same way. Most of the fields are standard, and we already learned it. For delivery address we give a field name. Then, we specify the type. And the type is `DeliveryAddress`. So the type of this field is the class name of the delivery address schema that we specified earlier. For `InvoiceLineItems` It is going to be an array of LineItem objects. So, it is a complex type that requires its own curly braces. Within the brace, we define the type as an array and the items as `LineItem`. This is how you define an array for Avro schema.
```json
{
  "namespace": "org.example.types",
  "type": "record",
  "name": "PosInvoice",
  "fields": [
    {"name": "InvoiceNumber","type": ["null","string"]},
    {"name": "CreatedTime","type": ["null","long"]},
    {"name": "CustomerCardNo","type": ["null","double"]},
    {"name": "TotalAmount","type": ["null","double"]},
    {"name": "NumberOfItems","type": ["null","int"]},
    {"name": "PaymentMethod","type": ["null","string"]},
    {"name": "TaxableAmount","type": ["null","double"]},
    {"name": "CGST","type": ["null","double"]},
    {"name": "SGST","type": ["null","double"]},
    {"name": "CESS","type": ["null","double"]},
    {"name": "StoreID","type": ["null","string"]},
    {"name": "PosID","type": ["null","string"]},
    {"name": "CashierID","type": ["null","string"]},
    {"name": "CustomerType","type": ["null","string"]},
    {"name": "DeliveryType","type": ["null","string"]},
    {"name": "DeliveryAddress","type": ["null","DeliveryAddress"]},
    {"name": "InvoiceLineItems","type": {"type": "array", "items": "LineItem"}}
  ]
}
```

We are done with the schema definitions now we have all the required schemas.

## How do we generate class definitions?

You can use Avro-maven-plugin. All we need is to add some details in your pom file. We'llneed the maven compiler plugin, and the  avro-maven-plugin.

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
                <!-- plugin's maven coordinates -->
                <groupId>org.apache.avro</groupId>
                <artifactId>avro-maven-plugin</artifactId>
                <version>1.8.1</version>
                <executions>
                    <!-- execution details -->
                    <execution>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>schema</goal>
                        </goals>
                        <configuration>
                            <!-- source directory for the schema definition files -->
                            <sourceDirectory>${project.basedir}/src/main/resources/schema/</sourceDirectory>
                            <!-- output directory where all the generated packages and classes would land-->
                            <outputDirectory>${project.basedir}/src/main/java/</outputDirectory>
                            <imports>
                                <!-- Since we're cross-referencing these 2 schemas, we need to import those files -->
                                <import>${project.basedir}/src/main/resources/schema/LineItem.avsc</import>
                                <import>${project.basedir}/src/main/resources/schema/DeliveryAddress.avsc</import>
                            </imports>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <!-- Avro dependency-->
        <dependency>
            <groupId>org.apache.avro</groupId>
            <artifactId>avro</artifactId>
            <version>1.8.1</version>
        </dependency>
    </dependencies>
```

That's all for the plugin. The build will generate an Avro source code, and you will need Avro packages to compile the generated code. So, we also need to include the Avro dependency.

Now we can jump to maven lifecycle and compile your project. Wait for a minute and done and all of the classes are generated. And the generated classes are serializable using the Confluent Avro serializer.

```java
public class DeliveryAddress extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -1574675994223781836L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"DeliveryAddress\",\"namespace\":\"org.example.types\",\"fields\":[{\"name\":\"AddressLine\",\"type\":[\"null\",\"string\"]},{\"name\":\"City\",\"type\":[\"null\",\"string\"]},{\"name\":\"State\",\"type\":[\"null\",\"string\"]},{\"name\":\"PinCode\",\"type\":[\"null\",\"string\"]},{\"name\":\"ContactNumber\",\"type\":[\"null\",\"string\"]}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.CharSequence AddressLine;
  @Deprecated public java.lang.CharSequence City;
  @Deprecated public java.lang.CharSequence State;
  @Deprecated public java.lang.CharSequence PinCode;
  @Deprecated public java.lang.CharSequence ContactNumber;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public DeliveryAddress() {}

  /**
   * All-args constructor.
   * @param AddressLine The new value for AddressLine
   * @param City The new value for City
   * @param State The new value for State
   * @param PinCode The new value for PinCode
   * @param ContactNumber The new value for ContactNumber
   */
  public DeliveryAddress(java.lang.CharSequence AddressLine, java.lang.CharSequence City, java.lang.CharSequence State, java.lang.CharSequence PinCode, java.lang.CharSequence ContactNumber) {
    this.AddressLine = AddressLine;
    this.City = City;
    this.State = State;
    this.PinCode = PinCode;
    this.ContactNumber = ContactNumber;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return AddressLine;
    case 1: return City;
    case 2: return State;
    case 3: return PinCode;
    case 4: return ContactNumber;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: AddressLine = (java.lang.CharSequence)value$; break;
    case 1: City = (java.lang.CharSequence)value$; break;
    case 2: State = (java.lang.CharSequence)value$; break;
    case 3: PinCode = (java.lang.CharSequence)value$; break;
    case 4: ContactNumber = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'AddressLine' field.
   * @return The value of the 'AddressLine' field.
   */
  public java.lang.CharSequence getAddressLine() {
    return AddressLine;
  }

  /**
   * Sets the value of the 'AddressLine' field.
   * @param value the value to set.
   */
  public void setAddressLine(java.lang.CharSequence value) {
    this.AddressLine = value;
  }

  /**
   * Gets the value of the 'City' field.
   * @return The value of the 'City' field.
   */
  public java.lang.CharSequence getCity() {
    return City;
  }

  /**
   * Sets the value of the 'City' field.
   * @param value the value to set.
   */
  public void setCity(java.lang.CharSequence value) {
    this.City = value;
  }

  /**
   * Gets the value of the 'State' field.
   * @return The value of the 'State' field.
   */
  public java.lang.CharSequence getState() {
    return State;
  }

  /**
   * Sets the value of the 'State' field.
   * @param value the value to set.
   */
  public void setState(java.lang.CharSequence value) {
    this.State = value;
  }

  /**
   * Gets the value of the 'PinCode' field.
   * @return The value of the 'PinCode' field.
   */
  public java.lang.CharSequence getPinCode() {
    return PinCode;
  }

  /**
   * Sets the value of the 'PinCode' field.
   * @param value the value to set.
   */
  public void setPinCode(java.lang.CharSequence value) {
    this.PinCode = value;
  }

  /**
   * Gets the value of the 'ContactNumber' field.
   * @return The value of the 'ContactNumber' field.
   */
  public java.lang.CharSequence getContactNumber() {
    return ContactNumber;
  }

  /**
   * Sets the value of the 'ContactNumber' field.
   * @param value the value to set.
   */
  public void setContactNumber(java.lang.CharSequence value) {
    this.ContactNumber = value;
  }

  /**
   * Creates a new DeliveryAddress RecordBuilder.
   * @return A new DeliveryAddress RecordBuilder
   */
  public static org.example.types.DeliveryAddress.Builder newBuilder() {
    return new org.example.types.DeliveryAddress.Builder();
  }

  /**
   * Creates a new DeliveryAddress RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new DeliveryAddress RecordBuilder
   */
  public static org.example.types.DeliveryAddress.Builder newBuilder(org.example.types.DeliveryAddress.Builder other) {
    return new org.example.types.DeliveryAddress.Builder(other);
  }

  /**
   * Creates a new DeliveryAddress RecordBuilder by copying an existing DeliveryAddress instance.
   * @param other The existing instance to copy.
   * @return A new DeliveryAddress RecordBuilder
   */
  public static org.example.types.DeliveryAddress.Builder newBuilder(org.example.types.DeliveryAddress other) {
    return new org.example.types.DeliveryAddress.Builder(other);
  }

  /**
   * RecordBuilder for DeliveryAddress instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<DeliveryAddress>
    implements org.apache.avro.data.RecordBuilder<DeliveryAddress> {

    private java.lang.CharSequence AddressLine;
    private java.lang.CharSequence City;
    private java.lang.CharSequence State;
    private java.lang.CharSequence PinCode;
    private java.lang.CharSequence ContactNumber;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(org.example.types.DeliveryAddress.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.AddressLine)) {
        this.AddressLine = data().deepCopy(fields()[0].schema(), other.AddressLine);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.City)) {
        this.City = data().deepCopy(fields()[1].schema(), other.City);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.State)) {
        this.State = data().deepCopy(fields()[2].schema(), other.State);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.PinCode)) {
        this.PinCode = data().deepCopy(fields()[3].schema(), other.PinCode);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.ContactNumber)) {
        this.ContactNumber = data().deepCopy(fields()[4].schema(), other.ContactNumber);
        fieldSetFlags()[4] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing DeliveryAddress instance
     * @param other The existing instance to copy.
     */
    private Builder(org.example.types.DeliveryAddress other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.AddressLine)) {
        this.AddressLine = data().deepCopy(fields()[0].schema(), other.AddressLine);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.City)) {
        this.City = data().deepCopy(fields()[1].schema(), other.City);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.State)) {
        this.State = data().deepCopy(fields()[2].schema(), other.State);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.PinCode)) {
        this.PinCode = data().deepCopy(fields()[3].schema(), other.PinCode);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.ContactNumber)) {
        this.ContactNumber = data().deepCopy(fields()[4].schema(), other.ContactNumber);
        fieldSetFlags()[4] = true;
      }
    }

    /**
      * Gets the value of the 'AddressLine' field.
      * @return The value.
      */
    public java.lang.CharSequence getAddressLine() {
      return AddressLine;
    }

    /**
      * Sets the value of the 'AddressLine' field.
      * @param value The value of 'AddressLine'.
      * @return This builder.
      */
    public org.example.types.DeliveryAddress.Builder setAddressLine(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.AddressLine = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'AddressLine' field has been set.
      * @return True if the 'AddressLine' field has been set, false otherwise.
      */
    public boolean hasAddressLine() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'AddressLine' field.
      * @return This builder.
      */
    public org.example.types.DeliveryAddress.Builder clearAddressLine() {
      AddressLine = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'City' field.
      * @return The value.
      */
    public java.lang.CharSequence getCity() {
      return City;
    }

    /**
      * Sets the value of the 'City' field.
      * @param value The value of 'City'.
      * @return This builder.
      */
    public org.example.types.DeliveryAddress.Builder setCity(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.City = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'City' field has been set.
      * @return True if the 'City' field has been set, false otherwise.
      */
    public boolean hasCity() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'City' field.
      * @return This builder.
      */
    public org.example.types.DeliveryAddress.Builder clearCity() {
      City = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'State' field.
      * @return The value.
      */
    public java.lang.CharSequence getState() {
      return State;
    }

    /**
      * Sets the value of the 'State' field.
      * @param value The value of 'State'.
      * @return This builder.
      */
    public org.example.types.DeliveryAddress.Builder setState(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.State = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'State' field has been set.
      * @return True if the 'State' field has been set, false otherwise.
      */
    public boolean hasState() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'State' field.
      * @return This builder.
      */
    public org.example.types.DeliveryAddress.Builder clearState() {
      State = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'PinCode' field.
      * @return The value.
      */
    public java.lang.CharSequence getPinCode() {
      return PinCode;
    }

    /**
      * Sets the value of the 'PinCode' field.
      * @param value The value of 'PinCode'.
      * @return This builder.
      */
    public org.example.types.DeliveryAddress.Builder setPinCode(java.lang.CharSequence value) {
      validate(fields()[3], value);
      this.PinCode = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'PinCode' field has been set.
      * @return True if the 'PinCode' field has been set, false otherwise.
      */
    public boolean hasPinCode() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'PinCode' field.
      * @return This builder.
      */
    public org.example.types.DeliveryAddress.Builder clearPinCode() {
      PinCode = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /**
      * Gets the value of the 'ContactNumber' field.
      * @return The value.
      */
    public java.lang.CharSequence getContactNumber() {
      return ContactNumber;
    }

    /**
      * Sets the value of the 'ContactNumber' field.
      * @param value The value of 'ContactNumber'.
      * @return This builder.
      */
    public org.example.types.DeliveryAddress.Builder setContactNumber(java.lang.CharSequence value) {
      validate(fields()[4], value);
      this.ContactNumber = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'ContactNumber' field has been set.
      * @return True if the 'ContactNumber' field has been set, false otherwise.
      */
    public boolean hasContactNumber() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'ContactNumber' field.
      * @return This builder.
      */
    public org.example.types.DeliveryAddress.Builder clearContactNumber() {
      ContactNumber = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    @Override
    public DeliveryAddress build() {
      try {
        DeliveryAddress record = new DeliveryAddress();
        record.AddressLine = fieldSetFlags()[0] ? this.AddressLine : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.City = fieldSetFlags()[1] ? this.City : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.State = fieldSetFlags()[2] ? this.State : (java.lang.CharSequence) defaultValue(fields()[2]);
        record.PinCode = fieldSetFlags()[3] ? this.PinCode : (java.lang.CharSequence) defaultValue(fields()[3]);
        record.ContactNumber = fieldSetFlags()[4] ? this.ContactNumber : (java.lang.CharSequence) defaultValue(fields()[4]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  private static final org.apache.avro.io.DatumWriter
    WRITER$ = new org.apache.avro.specific.SpecificDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  private static final org.apache.avro.io.DatumReader
    READER$ = new org.apache.avro.specific.SpecificDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}
```

