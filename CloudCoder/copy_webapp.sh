#! /bin/bash

# Copy the web application to a destination directory.
# By default, the destination directory is the "apps" subdirectory
# of the CloudCoderWebServer project, which should be a sibling
# of this (CloudCoder) project.

todir="$1"

if [ -z "$todir" ]; then
	todir=../CloudCoderWebServer/apps
fi

if [ ! -d "$todir" ]; then
	echo "Cannot find destination directory\n"
	exit 1
fi

# delete old webapp directory
(cd "$todir" && rm -rf cloudCoder)

# create a new webapp directory
mkdir -p "$todir/cloudCoder"

# copy the webapp
tar cf - war | (cd "$todir/cloudCoder" && tar xf - --strip-components=1)
