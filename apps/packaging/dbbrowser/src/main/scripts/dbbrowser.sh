#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/dbbrowser`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -cp "openxal.apps.dbbrowser-1.0.0-SNAPSHOT.jar:*" -DOPENXAL_CONF=${CODAC_CONF}/openxal xal.app.dbbrowser.Main
