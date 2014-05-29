#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/xyzcorrelator`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -jar "openxal.apps.xyzcorrelator-$OPENXAL_VERSION.jar"
