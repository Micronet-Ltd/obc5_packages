# Build instructions for testing

Run `mm WITH_DEXPREOPT=false -B` to just build the APK.

# Note on configuration files

Configuration files for the test order are stored under product_config in croot for each individual project name (as defined in the `PROJECT_NAME` variable defined when setting up environment).

# Note on battery capacity test

In order to configure the acceptable battery range, create a file called `battery_levels.csv` in the root directory of the SD card.  The contents of the file will be `<min_battery_level_>,<max_battery_level>`.
