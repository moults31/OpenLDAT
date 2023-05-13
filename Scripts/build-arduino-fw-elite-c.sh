#!/bin/bash

echo "FW build script invoked for Elite C..."
arduino-cli compile \
    -b SparkFun:avr:openldatEliteC \
    --build-path $FW_BUILD_PATH \
    --libraries $LIB_DIR \
    $FW_SRC_PATH