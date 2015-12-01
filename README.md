#compass-project

How to write an android compass app? Here's one way to do it

Features
-
- Compass showing current bearing to the north
- Mini-compass draggable floating screen widget
- Navigation: pick a destination and the app will display the bearing, distance, current speed and estimated reach time
- Decimal or DMS (degree, minute, second) coordinates input, with filter so invalid coordinates cannot be entered
- Optional place picker for choosing a destination on a map
- Metric or imperial unit system

Requirements
-
- Compass and accelerometer sensors
- Location and optional network provider for navigation
- Google Play Services for place picker

Building
-
When building from source, keep in mind that the API key for Place Picker (the com.google.android.geo.API_KEY in AndroidManifest.xml) will be invalid as long as SHA1 key applied to the build artifact won't match the one used to generate the API key, so another key must be generated with a proper SHA1 key to replace the one in manifest, or the place picker will fail silently. See https://developers.google.com/places/android-api/signup

Testing
-
Either build from source as specified above, or use the app-debug.apk included in the root directory.

Succesfully tested on Motorola XT1039 device with Android 5.1 Lollipop

![screenshot](http://s9.postimg.org/4848a6hhb/Screenshot_2015_09_23_23_06_51.png)
