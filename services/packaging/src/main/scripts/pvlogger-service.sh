#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/pvlogger-service`
echo $LINKTARGET
DIR=`dirname $LINKTARGET`
echo $DIR
cd $DIR/../lib/openxal && 
java -cp "openxal.service.pvlogger-1.0.0-SNAPSHOT.jar:*" xal.service.pvlogger.Main
