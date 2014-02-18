#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/opticseditor`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -cp "openxal.apps.opticseditor-1.0.0-SNAPSHOT.jar:*" xal.app.opticseditor.Main
