#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/scan1d`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -cp "openxal.apps.scan1d-1.0.0-SNAPSHOT.jar:*" xal.app.scan1d.MainScan1D
