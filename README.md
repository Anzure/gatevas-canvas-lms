# gatevas-canvas-lms
Tool for importing to Canvas LMS.

## Settings

Create your own application.properties with required fields filled out.

Make sure application.properties are in the resources folder.


## Available commands

Show a list of stored courses:
- course list

Add a course to storage:
- course add

Remove a course from storage:
- course remove

Show information about stored course:
- course info

Import students from Google Sheets:
- course import

Export missing students to Canvas User SIS CSV import-file:
- course export

Enroll students to course in Canvas:
- course enroll

Synchronize storage with data in Canvas:
- course sync

Add course to storage with JSON file:
- course legacy

Send email to students in course:
- course email

Send SMS to students in course:
- course sms
