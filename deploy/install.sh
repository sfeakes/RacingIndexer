#!/bin/sh
#
#ROOT=/root/test-install
#

LIB=$ROOT/usr/lib/racingindexer
CACHE=$ROOT/var/cache/racingindexer
BIN=$ROOT/usr/bin

NAME=racingindexer
SHNAME=racingIndexer.sh
PROPNAME=racingIndexer.prop

mkdir -p $LIB
mkdir -p $CACHE
mkdir -p $BIN
chmod 666 $CACHE

DEPLOYEDFILES='racingIndexer.jar racingIndexer.sh racingIndexer.prop.example'

for file in $DEPLOYEDFILES; do
  echo Getting $file
  curl -o "$LIB/$file" "https://raw.githubusercontent.com/sfeakes/RacingIndexer/master/deploy/$file"
done

echo 

# link bin to script
if [ -e $BIN/$NAME ]; then
  if [ ! -L $BIN/$NAME ]; then
    echo "Error $BIN/$NAME exists but is not a link to $LIB/$SHNAME"
  fi
else
  ln -s $LIB/$SHNAME $BIN/$NAME
fi

if [ ! -f $LIB/$PROPNAME ]; then
  echo "Please copy $LIB/$PROPNAME.example to $LIB/$PROPNAME and modify as needed."
fi