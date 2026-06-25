## What
We need to create a screen that shows the list of log sessions to the user with basic information like when it was started.

## Summary
- Ideally this should open as a separate UI and not something related to actual app's UI navigation.
- This could look like a separate activity for android, and for iOS a wrapping UI that opens a bottomsheet when the floating bubble is clicked.
- This base setup should have its own navigation setup through a central state handler instead of setting up another navigation library

## Sessions list screen
- This should observe the sessions list and show the list is descending order of their starting time
- Each session would have a click option too which for not can be left empty
- On toolbar of the screen, there should be an option of delete, which when clicked
  - should first prompt a confirmation dialog
  - when confirmed, it should delete all the sessions and their logs

## Logs display screen
- This should observe all the logs for the selected session and show them in descending order
- If current scroll position is already on the latest item, then when a new item comes on top, list should scroll to that
- If current scroll position is not on the latest item, then scroll position should not auto adjust
- We have a limit on logs in a session, so at the end of the logs, we should show the session start time
- each log item should display the following things
  - millis time
  - D/V/E/I/W -> for log levels with different color code for each
    - D - white
    - V - light blue
    - E - light red
    - I - light yellow
    - W - light orange
  - log tag
  - log message
  - log error info if present
- each item in the log item mentioned above, should be individually selectable so that someone can copy it
- there should be option to delete existing logs in the session -> this should not affect the session start time
- there should be option to filter the logs by log level - only one log level filter at a time

## Vertical Slices
1. Base setup (*)
   1. activity creation for the android + a floating button for now to open this (*)
   2. state based navigation system (*)
2. Sessions screen (*)
   1. Sessions display (*)
3. Logs display screen
   1. Logs display -> simply display in descending order, should show logs in simple text without color coding (*)
   2. Logs card styling -> log color coding, fields selectable and copiable (*)
   3. Logs scroll position handling (*)
   4. Filter logs by log level 
   5. Delete logs for a session

## Post all slices
1. how does the viewmodel and other job cleanup works when moving from logs screen to sessions screen
2. logs time in readable format
3. theme adjustment for library