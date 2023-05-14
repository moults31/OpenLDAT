#!/bin/bash

echo "Clean script invoked."
echo "Removing everything at path $FW_BUILD_PATH..."
rm -rf $FW_BUILD_PATH/*
echo "Clean script done."
