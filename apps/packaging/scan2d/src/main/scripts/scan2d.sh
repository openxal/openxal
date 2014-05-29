#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/scan2d`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -jar "openxal.apps.scan2d-$OPENXAL_VERSION.jar"
