#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/bricks`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -cp "openxal.apps.bricks-1.0.0-SNAPSHOT.jar:*" xal.app.bricks.Main
