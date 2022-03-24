# Joins

## What's a join?
- Joining means taking a KStream and/or KTable and creating a new KStream or KTable from it.

- There are 4 kind of joins (SQL-like), and the most common one will be analyzed in a further section, including behavior and usage

<table>
  <tr>
    <th>Join operands</th>
    <th>Туре</th>
    <th>(INNER) JOIN</th>
    <th>LEFT JOIN</th>
    <th>OUTER JOIN</th>
  </tr>
<tr>
    <td>KStream-to-KStream</td>
    <td>Windowed</td>
    <td style="background-color:green">Supported</td>
    <td style="background-color:green">Supported</td>
    <td style="background-color:green">Supported</td>
  </tr>
<tr >
    <td>KTable-to-KTable</td>
    <td>Non-windowed</td>
    <td style="background-color:green">Supported</td>
    <td style="background-color:green">Supported</td>
    <td style="background-color:green">Supported</td>
  </tr>
  <tr>
    <td>KStream-to-KTable</td>
    <td>Non-windowed</td>
    <td style="background-color:green">Supported</td>
    <td style="background-color:green">Supported</td>
    <td style="background-color:red">Not Supported</td>
  </tr>
  <tr>
    <td>KStream-to-GlobalKTable</td>
    <td>Non-windowed</td>
    <td style="background-color:green">Supported</td>
    <td style="background-color:green">Supported</td>
    <td style="background-color:red">Not Supported</td>
  </tr>
   <tr >
    <td>KTable-to-GlobalKTable</td>
    <td>N/A</td>
    <td style="background-color:red">Not Supported</td>
    <td style="background-color:red">Not Supported</td>
    <td style="background-color:red">Not Supported</td>
  </tr>
</table>

# Join Constraints
## Co-partitioning of data

- These 3 joins:
  - KStream / KStream
  - KTable / KTable
  - KStream / KTable
- Can only happen when the data is co-partitioned. Otherwise the join
won't be doable and Kafka Streams will fail with a `Runtime Error`
- That means that the `same number of partitions` is there on the stream and
 / or the table.
- To co-partition data, if the number of partitions is different, `write back the
topics through` Kafka before the join. This has a network cost.

# GlobalKTable

- If your KTable data is reasonably small, and can fit on each of your Kafka Streams application, you can read it is a `GlobalKTable`
- With GlobalKTables, you can join any stream to your table even if the
data doesn't have the same number of partition
- That's because the table data lives on every Streams application
instance
- The downside is size on disk, but that's okay for reasonably sized
dataset
