#!/bin/bash
FIRMWARE=./OpenLDAT.ino.leonardo.hex
if [ $# -ne 1 ]; then
    echo "Usage: flash.sh /dev/device"
    echo "Replace device with your target device (absolute path)"
    echo "Use ls -l /dev/serial/by-id to get a list of serial devices (usually ttyACM0, ttyUSB0, etc.)"
    exit 10
fi
if [ "$EUID" -ne 0 ]; then
    echo "This script must be run as root (or with sudo)"
    exit 1
fi
if ! command -v python &> /dev/null
then
    echo "Python is not installed or is not in the PATH"
    exit 2
fi
if ! command -v avrdude &> /dev/null
then
    echo "avrdude is not installed or is not in the PATH"
    exit 3
fi
full_path=$(realpath $0)
dir_path=$(dirname "$full_path")
cd "$dir_path"
if ! [ -e "$1" ]; then
    echo "Device $1 not found"
    exit 4
fi
if ! [ -f "$FIRMWARE" ]; then
    echo "Firmware hex file not found"
    exit 5
fi
echo "Resetting the device..."
cat << EOF > /tmp/reset32u4.py
#!/usr/bin/env python
import serial, sys
serialPort = sys.argv[1]
ser = serial.Serial(
    port=serialPort,
    baudrate=1200,
    parity=serial.PARITY_NONE,
    stopbits=serial.STOPBITS_ONE,
    bytesize=serial.EIGHTBITS
)
ser.isOpen()
ser.close()

EOF
python /tmp/reset32u4.py $1
if [ $? -ne 0 ]; then
    echo "Something went wrong"
    exit 6
fi
rm -f /tmp/reset32u4.py
sleep 2
echo "Flashing..."
avrdude -v -patmega32u4 -cavr109 -P$1 -b57600 -D -Uflash:w:$FIRMWARE:i
if [ $? -ne 0 ]; then
    echo "Flashing failed (error $?)"
    exit 7
fi
echo "Success, you now have an OpenLDAT device!"
exit 0
