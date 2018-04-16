# Build instructions for testing

Build instructions can be found on [the TeamBook.](https://micronet1023744.sharepoint.com/:o:/r/_layouts/15/WopiFrame.aspx?sourcedoc={0122c127-efe4-4a62-acdd-2b1bed3eeeb8}&action=edit&wd=target%28%2F%2FOBC5.one%7C2e2d56f6-faef-4026-8438-a150ca8e239c%2FOS%20Build%20Instructions%7C1f483dbe-5d80-4e1a-9a54-6f48014582a6%2F%29)
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

In order to configure the acceptable battery range, create a file called `battery_levels.csv` in the root directory of the SD card.
The contents of the file will be `<min_battery_level_>,<max_battery_level>`.
