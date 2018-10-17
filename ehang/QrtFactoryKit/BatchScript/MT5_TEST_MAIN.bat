@echo off
rem ******
rem change the following line to select a different language!
rem ******
call Input\English.bat
setlocal
cd /d %~dp0
set scriptversion=2.1.0
del /F /Q temp.csv 1>nul 2>nul
del /F /Q temp2.csv 1>nul 2>nul
if not exist Results mkdir Results
echo ************************************
echo %messageIntro% %scriptversion%
echo ************************************
echo %messageConnectUSB%

for /F "eol=; tokens=1,2,3* delims=," %%i in (Input\partNumber.dat) do (
if %1==%%i (
    set ramParam=%%j
	set flashParam=%%k
)
)
pause
adb wait-for-device
adb pull /storage/sdcard0/test_results.csv temp.csv 1>nul 2>nul
set nt="N/A"
set /p tempfile=<temp.csv
if %tempfile:~0,1% NEQ 3 (set /p param1=%messageEnterSN%) else (set param1=%nt%)
if %tempfile:~0,1% EQU 3 (set /p param4=%messageEnterCSN%) else (set param4=%nt%)
if %tempfile:~0,1% NEQ 3 (set /p param2=%messageEnterIMEI%) else (set param2=%nt%) 

if not exist temp.csv goto :testnotrun
for /F "tokens=1 delims=," %%a in (Input\OS_VERSION.dat) do (set param3=%%a)
for /F "tokens=1,2,3,4,5,6 delims=," %%a in ("%tempfile:~2%") do @(
if !param1! NEQ !nt! @(
    if %%a == %param1% (set param1=Pass) else (set param1=Fail) || set param1=Fail 
) else set param1=!nt!
if %%b == %param2% (set param2=Pass) else (set param2=Fail) || set param2=Fail
if %%c == %param3% (set param3=Pass) else (set param3=Fail) || set param3=Fail
if %%e == %ramParam% (set memoryPass=Pass) else (set memoryPass=Fail) || set memoryPass=Fail
if %%f == %flashParam% (set memoryPass=Pass) else (set memoryPass=Fail) || set memoryPass=Fail
)


if %tempfile:~0,1% EQU 3 (
    goto :cradleonly
)
if not exist Results\full_test_summary.csv copy Input\full_test_template.csv Results\full_test_summary.csv >nul
for /F "tokens=1,2,3,4,5,6* delims=," %%a in ("%tempfile:~2%") do echo %DATE%,%TIME%,%%a,%param1%,%%b,%param2%,%%c,%param3%,%%d,%%e,%%f,%memoryPass%,%%g,%scriptversion% >temp2.csv
findstr Fail temp2.csv 1>nul 2>nul
if %ERRORLEVEL%==0 @(
  for /F "tokens=* delims=," %%a in (temp2.csv) do echo %%a,Fail >>Results\full_test_summary.csv
  color 47
) else (
  for /F "tokens=* delims=," %%a in (temp2.csv) do echo %%a,Pass >>Results\full_test_summary.csv
  color 27
)

del /F /Q temp.csv 1>nul 2>nul
del /F /Q temp2.csv 1>nul 2>nul
waitfor /T 1 a 2>nul
color 07
goto :eof
:testnotrun
echo %messageTestNotFound%
pause
goto :eof
:cradleonly
if not exist Results\cradle_only_summary.csv copy Input\cradle_only_template.csv Results\cradle_only_summary.csv >nul
for /F "tokens=1,2,3,4,5,6,7,8 delims=," %%a in ("%tempfile:~2%") do (
  echo %DATE%,%TIME%,%%c,%param4%,%%d,%%e,%%f,%%g,%%h,%scriptversion% >temp2.csv
)
findstr Fail temp2.csv 1>nul 2>nul
if %ERRORLEVEL%==0 @(
for /F "tokens=* delims=," %%a in (temp2.csv) do echo %%a,Fail >>Results\cradle_only_summary.csv
color 47
) else (
for /F "tokens=* delims=," %%a in (temp2.csv) do echo %%a,Pass >>Results\cradle_only_summary.csv
color 27
)
del /F /Q temp.csv 1>nul 2>nul
del /F /Q temp2.csv 1>nul 2>nul
waitfor /T 1 a 2>nul
color 07
goto :eof
