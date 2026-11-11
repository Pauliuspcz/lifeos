**MISSING:**
Goal-service has a PUT, but publishes no event, Report takes
period and target from each goal's latest check-in. 
so if a user changes goal from 3/week to 5/week, report keeps using
the 3 until the next check-in.

2 Choices:

1. Ignore and keep it as design choice, if a user reverts it back to 3, it stays the same if no progress has been listed
2. Goal-service contacts report service asynchronously, for goal.updated event, and opens the possibility to also add goal.archived og/and goal.deleted.

Downside of adding this would mean a change in the arbeidskrav project architecture and would need an explanation. 