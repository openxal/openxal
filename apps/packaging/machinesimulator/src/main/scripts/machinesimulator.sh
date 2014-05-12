#!/bin/bash
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/machinesimulator`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -jar "openxal.apps.machinesimulator-1.0.1-SNAPSHOT.jar"
