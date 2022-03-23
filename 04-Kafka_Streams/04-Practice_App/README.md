# Practice Exercise
## Favourite Color

- Take a comma delimited topic of userid,colour
  - Filter out bad data
  - Keep only color of "green", "red" or "blue"
- Get the running count of the favourite colours overall and output this to a topic
- A user's favourite color can change

Example:   
- alice, blue
- bob, green
- alice, red (update here)
- charlie, red

| color | count |
|-------|-------|
|blue   |   0   |
| green |   1   |
|red    |   2   |

## Guidance 

- Write topology
- Start finding the right transformations to apply (see previous section)
- Creating input and output topics (and intermediary topics if you think
of any)
- Feed the sample data as a producer:
    - alice, blue
    - bob, green
    - alice, red 
    - charlie, red