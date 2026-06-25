## What
We need to add a filter by tag option in our logs screen.

## Current state
Currently while showing the logs, we have the option to filter by logLevel.
We need to add a filter by tag also, which when passed should filter the logs based on the tag, if no logs by that tag, then it will yield no logs state

## Feature description
- Clicking on filter option, current bottomsheet should open
- Filter sheet should have a text field where a user can enter the tag name
- Same sheet should have an apply button, anytime user selects any log level or tag or both, clicking on apply should actually apply the filters
- Both the filter operations, level and string should work together, for a log to qualify, tag and level both should match
- When tag field is empty string, then it should be considered as not filtered, ie. all the logs irrespective of tag constraint should display while log level condition should still apply if applicable