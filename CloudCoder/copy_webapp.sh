#! /bin/bash

# Copy the web application to a destination directory.

todir="$1"

if [ -z "$todir" ]; then
	echo "Usage: ./copy_webapp.sh <todir>"
	exit 1
fi

mkdir -p $todir/cloudCoder
tar cf - war | (cd $todir/cloudCoder && tar xf - --strip-components=1)
