#! /bin/bash

# Copy the web application to a destination directory.
# By default, the destination directory is the "apps" subdirectory
# of the CloudCoderWebServer project, which should be a sibling
# of this (CloudCoder) project.

todir="$1"

if [ -z "$todir" ]; then
	todir=../CloudCoderWebServer/apps
fi

mkdir -p $todir/cloudCoder
tar cf - war | (cd $todir/cloudCoder && tar xf - --strip-components=1)
