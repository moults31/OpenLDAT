#!/bin/zsh

brew install arduino-cli
brew install dfu-programmer

arduino-cli config init --additional-urls https://raw.githubusercontent.com/sparkfun/Arduino_Boards/main/IDE_Board_Manager/package_sparkfun_index.json
arduino-cli core update-index
arduino-cli core install sparkfun:avr
arduino-cli core install arduino:avr


export ROOT_DIR=$PWD/..
export LIB_ZIP_PATH=$ROOT_DIR/Device/Firmware/Libraries.zip
export LIB_DIR=$ROOT_DIR/Device/Firmware/Libraries
export FW_ROOT_PATH=$ROOT_DIR/Device/Firmware
export FW_SRC_PATH=$FW_ROOT_PATH/OpenLDAT
export FW_BUILD_PATH=$FW_SRC_PATH/build
export APP_ROOT_PATH=$ROOT_DIR/App/OpenLDAT
export APP_BUILD_PATH=$APP_ROOT_PATH/build
export APPIMAGE_PATH=$HOME/Applications

cp $FW_ROOT_PATH/boards.txt ~/Library/Arduino15/packages/SparkFun/hardware/avr/1.1.13/boards.local.txt

unzip -q $LIB_ZIP_PATH -d $FW_ROOT_PATH
