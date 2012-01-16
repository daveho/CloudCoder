#! /bin/bash

if [ $# -lt 1 ]; then
	echo "Usage: ./start.sh <CloudCoder webapp server> [<CloudCoder web app port>]"
	exit 1
fi

./_launchCloudCoderBuilder.pl "$@" >> log.txt 2>&1 &
