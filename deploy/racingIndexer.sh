#!/bin/sh

APP_ROOT="/usr/lib/racingindexer"
APP_CFG="$APP_ROOT/racingIndexer.prop"

# add APP_ROOT to LD_LIBRARY_PATH
if [ ! -z "$LD_LIBRARY_PATH" ]; then
  export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:$APP_ROOT"
else
  export LD_LIBRARY_PATH="$APP_ROOT"
fi

case $1 in
  -h | --help | help | -v | --version)
    /usr/bin/java -jar "$APP_ROOT/racingIndexer.jar" -h
  exit 0
;;  
esac

/usr/bin/java -jar "$APP_ROOT/racingIndexer.jar" -cfg "$APP_CFG" "$@"