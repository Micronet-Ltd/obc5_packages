# Build instructions for testing

Run `mm WITH_DEXPREOPT=false -B` to just build the APK.

# Installation instructions

Because QrtFactoryKit is a system app that is not intended to be used by users, the standard install commands with adb do not work.
In order to install the app on your MT5 device, you will need to run the following commands:

```
adb root
adb remount
adb push QrtFactoryKit.apk /system/app/QrtFactoryKit/QrtFactoryKit.apk
adb reboot
```

# Note on configuration files

Configuration files for the test order are stored under product_config in croot for each individual project name (as defined in the `PROJECT_NAME` variable defined when setting up environment).

# Note on battery capacity test

In order to configure the acceptable battery range, create a file called `battery_levels.csv` in the root directory of the SD card.  The contents of the file will be `<min_battery_level_>,<max_battery_level>`.
