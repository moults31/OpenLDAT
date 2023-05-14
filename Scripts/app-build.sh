#!/bin/bash

echo "App build script invoked..."
ant -Duser.properties.file=$HOME/.properties -f $APP_ROOT_PATH