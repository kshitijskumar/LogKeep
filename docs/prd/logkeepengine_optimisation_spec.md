## What
LogKeepEngine is responsible for logging data to DB, creating and manage session.
Currently the impl is a simple impl doing the job its supposed to do, but there are 2 optimisations we can look at

## Task 1
### Summary
Currently whenever log is called it launches a scope and then starts inserting data to DB, the issue is if multiple
log function gets called simultaneously then it can happen that the order of logs will get mixed up depending
which job gets executed first. Ideally this should not happen and the order of execution should remain as is.
We need to optimise log function to ensure that the order the log function gets called is the order of logs getting saved.


### Task 2
### Summary
Currently log function directly starts inserting data in DB, this is fine for initial project, but if multiple
log functions starts getting called then all of them will keep on inserting and incrementing and so on multiple times in a second.
Ideally we should have some form of batch update type process, that ensures tha db operation is not run on every log call and only update in a batch,
but also that no logs are missed.