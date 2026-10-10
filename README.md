# Tenant Management System

Navigation and Intents.

I added Login and Register screens, passed the registered email using Intent
extras, and connected Login to the Add Tenant screen. I also implemented
required-field validation, a welcome message, a website help link, and actions
to dial a saved tenant or share their details.

The app uses View Binding and Data Binding. It is a navigation practical,
so login only checks for nonblank fields; accounts and tenants are not stored
in a database.

**Build:** `.\gradlew.bat assembleDebug`  
**Checks:** `.\gradlew.bat testDebugUnitTest lintDebug`  

