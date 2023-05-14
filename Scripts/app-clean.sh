#!/bin/bash

echo "App clean script invoked.."
ant -Duser.properties.file=$HOME/.properties clean -f $APP_ROOT_PATH