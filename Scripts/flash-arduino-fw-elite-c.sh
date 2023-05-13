#!/bin/bash

echo "Flash script invoking dfu-programmer for Elite C..."

dfu-programmer atmega32u4 erase
dfu-programmer atmega32u4 flash $FW_BUILD_PATH/OpenLDAT.ino.hex