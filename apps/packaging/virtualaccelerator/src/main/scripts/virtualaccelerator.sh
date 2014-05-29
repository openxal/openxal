#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/virtualaccelerator`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -jar "openxal.apps.virtualaccelerator-$OPENXAL_VERSION.jar" -Dcom.cosylab.epics.caj.CAJContext.addr_list=127.0.0.1 -Dcom.cosylab.epics.caj.CAJContext.auto_addr_list=false
