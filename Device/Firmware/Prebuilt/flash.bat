@echo off
setlocal

set FIRMWARE="%~dp0\OpenLDAT.ino.leonardo.hex"

set /A ARGS_COUNT=0
for %%A in (%*) do set /A ARGS_COUNT+=1
if %ARGS_COUNT%==1 goto step1
echo Usage: flash.bat COM1
echo Replace COM1 with the device you want to flash
echo List of currently available COM ports:
for /f "tokens=1* delims==" %%I in ('wmic path win32_pnpentity get caption  /format:list ^| findstr "(COM[0-9]*)"') do (
    echo %%~J
)
goto exit

:step1
set avrdudepath=NUL
if exist "%programfiles(x86)%\Arduino\hardware\tools\avr\bin\avrdude.exe" set avrdudepath="%programfiles(x86)%\Arduino\hardware\tools\avr\bin"
if exist "%programfiles%\Arduino\hardware\tools\avr\bin\avrdude.exe" set avrdudepath="%programfiles%\Arduino\hardware\tools\avr\bin"
if %avrdudepath%==NUL goto noavrdude
if not exist %FIRMWARE% goto nofwhex

:step2
echo Resetting the device...
mode %1: BAUD=1200 parity=N data=8 stop=1
if %ERRORLEVEL%==0 goto step3
echo Failed, make sure you're using the correct COM port
goto exit

:step3
timeout /t 2 /nobreak > NUL
for /f "tokens=1* delims==" %%I in ('wmic path win32_pnpentity get caption  /format:list ^| find "Arduino Leonardo bootloader"') do (
	call :flash "%%~J"
)
echo Failed: the device did not enter bootloader mode
goto exit

:flash <device>
setlocal
set "str=%~1"
set "num=%str:*(COM=%"
set "num=%num:)=%"
set port=COM%num%
echo Flashing...
pushd "%avrdudepath%"
avrdude.exe -v -C..\etc\avrdude.conf -patmega32u4 -cavr109 -P%port% -b57600 -D -Uflash:w:%FIRMWARE%:i
if %ERRORLEVEL%==0 goto success
echo Flashing failed
goto exit

:noavrdude
echo Arduino IDE is not installed
goto exit

:nofwhex
echo Firmware file is missing
goto exit

:success
echo Success! You now have an OpenLDAT device
goto exit

:exit
pause
exit
