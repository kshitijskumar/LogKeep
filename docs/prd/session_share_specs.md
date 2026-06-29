## What
We need to build a share session feature. This should allow user to share all the captured logs of the session in a txt file to someone else.

## Feature description
- each session should have a option to share the logs
- whenever user chooses the option, the current session and logs details should be put in a file and system share mechanism should trigger
- there is no need of make the file accessible to user from their file system - to reduce complexity
- the file should not apply any filtering logic, filter is only for app display, logs file should share everything

## File format proposed
```text
Session started at: <time in hh:mm:ss> - <MMM dd YYYY>
Total logs: <count of logs in session>
\n
\n
<time in hh:mm:ss> - <LogLevel> - <tag> - <message>
<stacktrace if available>
\n
// next log
\n
// next log

```

## Tasks
1. file creation
2. share option and share feature in logs display screen