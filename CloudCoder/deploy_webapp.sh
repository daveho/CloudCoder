#! /bin/bash

# A simple script to deploy the webapp to a remote server.
# Should make this more easily configurable.

hostname=cs.ycp.edu
todir=/home/dhovemey/work/CloudCoder/CloudCoderWebServer/apps

echo -n "Removing old CloudCoder app..."
ssh $hostname "cd $todir && rm -rf cloudCoder"
echo "done"

echo -n "copying webapp..."
tar cf - war | ssh $hostname "cd $todir && mkdir -p cloudCoder && cd cloudCoder && tar xf - --strip-components=1"
echo "done"
