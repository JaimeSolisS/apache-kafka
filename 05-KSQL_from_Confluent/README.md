# KSQL from Confluent

- Source Available project at https://www.confluent.io/ksql
- Writing Kafka Streams Java applications is complex
- Sometimes, you just want to write... SQL!
```sql
CREATE STREAM vip_actions AS
    SELECT userid, page, action
    FROM clickstream c
    LEFT JOIN users u ON c.userid = u.user_id
    WHERE u.level = 'Platinum';
```
- Underneath, Kafka Streams applications are generated
- So we get the same benefits (scale, security)

# Contents 
- Download, install & setup a KSQL environment
- Build a KSQL project using streams, tables and joins
- KSQL data encoding, manipulation and enrichment
- Time based window concepts
- Advanced data handling
  - Geospatial and User Defined Functions
- Moving to production
  - Load balancing and horizontal scaling