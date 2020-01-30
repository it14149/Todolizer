# Todolizer

3 Activities used. 
MainActivity with RecyclerView of items
TaskActivity for creating tasks
ShowActivity for showing and editing tasks, accessible by clicking on task items in MainActivity

2 Fragments used.
CalendarFragment and TimeFragment when creating a task
They are created by CalendarActivity and communicate with it with interfaces

An external library was used (Otto) for an event bus to communicate all changes from ShowActivity with the adapter
All events are created in Events class
The adapter "subscribes" to a bus and listens for events that are posted when a change is made (in ShowTaskActivity)


SQLite db used as local database, all handled in a separate singleton class

A content provider was used when loading events from other calendars on the device
