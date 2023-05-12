#!/bin/bash

if [ -d /openldat ]; then
    source /openldat/env.sh
    echo "Success! Found $ROOT_DIR"
    cp $FW_ROOT_PATH/boards.txt ~/.arduino15/packages/SparkFun/hardware/avr/1.1.13/boards.local.txt
    
    if [ ! -d "$LIB_DIR" ]; then
        echo "Arduino Libarary path not found: $LIB_DIR"
        echo "Inflating $LIB_ZIP_PATH"
        unzip -q $LIB_ZIP_PATH -d $FW_ROOT_PATH
    fi

    exec gosu $(whoami) dumb-init "$@"
else
    echo "Fatal! User must pass -v </path/to/openldat/repo>:$DIRECTORY"
    exit 1
fi