#!/bin/sh

if [ -z "$1" ]; then
	echo "Usage: copyPackages.sh destination_dir"
	exit 0
fi

for i in `find -wholename '*packaging*/*.rpm'`
do 
	cp $i $1 
done
