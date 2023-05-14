#!/bin/bash

echo "Flash script invoking dfu-programmer for Elite C."
echo "Note: This script REQUIRES sudo for accessing the device!"

dfu-programmer atmega32u4 erase
dfu-programmer atmega32u4 flash $FW_BUILD_PATH/OpenLDAT.ino.hex