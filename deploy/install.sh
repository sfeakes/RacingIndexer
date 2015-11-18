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

# download git deploy directory for lib
#git https://github.com/sfeakes/RacingIndexer/tree/master/deploy
#git clone ssh://host/repo.git ~/

for file in $DEPLOYEDFILES; do
#wget "https://github.com/sfeakes/RacingIndexer/blob/master/deploy/$file?raw=true" -O "$LIB/$file"
echo Getting $file
curl -o "$LIB/$file" "https://github.com/scrakes/RacingIndexer/blob/master/deploy/$file?raw=true"
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

if [ ! -f $LIB/$PROP ]; then
echo "Please copy $LIB/$PROPNAME.example to $LIB/$PROPNAME and modify as needed."
fi