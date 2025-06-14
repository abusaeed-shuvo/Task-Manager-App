# Task Manager App

This project is made for `ES APP DEV 2404` course
in [Creative IT Institute]("https://www.creativeitinstitute.com/).

- App Icon: ![app_icon_24.png](app_icon_24.png)

In order to use Material Dynamic color this code has been added:

```bash
class MyApp : Application() {
override fun onCreate() {
super.onCreate()
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
DynamicColors.applyToActivitiesIfAvailable(this)
}}}
```

If you get red underline in app level gradle script at `targetSdk = 36` don't worry the app will
compile it is just a warning not error.

## Features

- Sqlite Database with Room persistence library and CRUDE Operation.
- Search and Sort Operation.
- Proper Dark mode integration.
- Material 3 Design.
- Android 16 support.

## Installation

You will need a computer with properly
installed [Android Studio](https://developer.android.com/studio) if you want to build this app
yourself.
If you just want to try the app you can download latest apk
package [here](https://github.com/abusaeed-shuvo/Task-Manager-App)

### Install the app from Android Studio:

1. Download the source code from github or copy the source link.
2. Open the file in android studio or select open from version control and paste the link.
3. Press Run button ![img.png](run.png)

Congratulation it will open in Emulator or phone.

### Install app from apk package:

1. Download the apk file from latest release
2. Open the file from a file manager in your phone
3. Give required permission to install the app
4. Open the App

## Screenshot of the app:

- ![app1.png](app1.png)
- ![app2.png](app2.png)

## Author

- [@abusaeed-shuvo](https://github.com/abusaeed-shuvo)

