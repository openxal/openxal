#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/xyzcorrelator`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -cp "openxal.apps.xyzcorrelator-1.0.0-SNAPSHOT.jar:*" xal.app.xyzcorrelator.Main
