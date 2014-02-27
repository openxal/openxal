#!/bin/bash 
CURRENT_DIR=`dirname $0`
LINKTARGET=`readlink -f $CURRENT_DIR/virtualaccelerator`
DIR=`dirname $LINKTARGET`
cd $DIR/../lib/openxal && 
java -cp "openxal.apps.virtualaccelerator-1.0.0-SNAPSHOT.jar:*" -Dcom.cosylab.epics.caj.CAJContext.addr_list=127.0.0.1 -Dcom.cosylab.epics.caj.CAJContext.auto_addr_list=false -DOPENXAL_CONF=${CODAC_CONF}/openxal xal.app.virtualaccelerator.Main
