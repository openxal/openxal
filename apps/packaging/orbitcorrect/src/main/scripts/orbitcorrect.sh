#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/orbitcorrect`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -jar "openxal.apps.orbitcorrect-$OPENXAL_VERSION.jar"
