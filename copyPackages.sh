#!/bin/sh

if [ -z "$1" ]; then
	echo "Usage: copyPackages.sh destination_dir"
	exit 0
fi

cp ./packaging/target/codac-core-4.1-openxal-core-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/opticseditor/target/codac-core-4.1-openxal-opticseditor-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/knobs/target/codac-core-4.1-openxal-knobs-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/xyzcorrelator/target/codac-core-4.1-openxal-xyzcorrelator-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/virtualaccelerator/target/codac-core-4.1-openxal-virtualaccelerator-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/bricks/target/codac-core-4.1-openxal-bricks-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/scan1d/target/codac-core-4.1-openxal-scan1d-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/scope/target/codac-core-4.1-openxal-scope-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/orbitcorrect/target/codac-core-4.1-openxal-orbitcorrect-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/pvhistogram/target/codac-core-4.1-openxal-pvhistogram-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/scan2d/target/codac-core-4.1-openxal-scan2d-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/mtv/target/codac-core-4.1-openxal-mtv-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/pvlogger/target/codac-core-4.1-openxal-pvlogger-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/machinesimulator/target/codac-core-4.1-openxal-machinesimulator-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/dbbrowser/target/codac-core-4.1-openxal-dbbrowser-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/launcher/target/codac-core-4.1-openxal-launcher-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/opticsswitcher/target/codac-core-4.1-openxal-opticsswitcher-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./apps/packaging/extlatgenerator/target/codac-core-4.1-openxal-extlatgenerator-4.1.0.v1.0-1.el6.x86_64.rpm $1
cp ./services/pvlogger/packaging/target/codac-core-4.1-openxal.service.pvlogger-4.1.0.v1.0-1.el6.x86_64.rpm $1

