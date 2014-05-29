#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/pvlogger`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -cp "openxal.apps.score-$OPENXAL_VERSION.jar:*" -DOPENXAL_CONF=${CODAC_CONF}/openxal xal.app.score.Main
