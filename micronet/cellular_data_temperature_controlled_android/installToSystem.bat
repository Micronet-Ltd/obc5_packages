REM CHANGE THESE
SET app_package=micronet.com.cellular_data_temperature_controlled
SET dir_app_name=Cellular_Data_Temperature_Controlled
SET main_activity=MainActivity

SET "adb_sh=adb shell"
SET "path_sysapp=/system/priv-app"
SET "apk_host=.\app\build\outputs\apk\app-debug.apk"
SET "apk_name=%dir_app_name%.apk"
SET "apk_target_dir=%path_sysapp%/%dir_app_name%"
SET "apk_target_sys=%apk_target_dir%/%apk_name%"

REM Install APK: using adb root
adb root
adb remount
adb push %apk_host% %apk_target_sys%
echo %apk_host%
echo %apk_target_sys%
REM Give permissions
%adb_sh% "chmod 755 %apk_target_dir%"
%adb_sh% "chmod 644 %apk_target_sys%"

REM Unmount system
%adb_sh% "mount -o remount,ro /"

echo DONE. PRESS ANY KEY TO REBOOT DEVICE.
PAUSE
adb reboot
