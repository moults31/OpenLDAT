#!/bin/bash

# Purpose: Set environment variables used in build scripts.
# Usage: source env.sh

export ROOT_DIR=$(dirname $(readlink -f "${BASH_SOURCE[0]}"))

export LIB_ZIP_PATH=$ROOT_DIR/Device/Firmware/Libraries.zip
export LIB_DIR=$ROOT_DIR/Device/Firmware/Libraries

export FW_ROOT_PATH=$ROOT_DIR/Device/Firmware
export FW_SRC_PATH=$FW_ROOT_PATH/OpenLDAT
export FW_BUILD_PATH=$FW_SRC_PATH/build

export APP_ROOT_PATH=$ROOT_DIR/App/OpenLDAT
export APP_BUILD_PATH=$APP_ROOT_PATH/build

export APPIMAGE_PATH=$HOME/Applications