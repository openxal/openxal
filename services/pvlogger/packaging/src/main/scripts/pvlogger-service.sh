#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/pvlogger-service`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java $1 -cp "openxal.service.pvlogger-$OPENXAL_VERSION.jar:*" xal.service.pvlogger.Main
