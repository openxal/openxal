#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/pvlogger`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -jar "openxal.apps.pvlogger-$OPENXAL_VERSION.jar"
