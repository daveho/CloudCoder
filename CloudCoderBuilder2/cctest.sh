#! /bin/bash

# Script for starting/stopping a builder on one of the cluster
# machines.

if [ $# -eq 0 ]; then
	echo "Usage:"
	echo "  ./cctest.sh <cmd>"
	exit 1
fi

instanceName=$(hostname)

# Invoke the builder, using an instance name and log filename
# prefix based on the hostname.  This will allow multiple builders
# to coexist in the same (shared) directory.
java -jar cloudcoderBuilder.jar \
	--jvmOptions="-Dbuilder2.log.prefix=$instanceName-" \
	--instance=$instanceName \
	--stdoutLog=log/$instanceName-stdout.log \
	"$@"
