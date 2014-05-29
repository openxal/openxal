#!/bin/bash
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/machinesimulator`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -jar "openxal.apps.machinesimulator-$OPENXAL_VERSION.jar"
