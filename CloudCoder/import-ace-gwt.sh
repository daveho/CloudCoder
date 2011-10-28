#! /bin/bash

# Run this script to update to the latest AceGWT code
# from the github repository

echo -n "Removing old AceGWT code..."
(cd src && rm -rf src/edu/ycp/cs/dh)
echo "done"

echo -n "Downloading new AceGWT code..."
(cd src && \
	(wget --no-check-certificate https://github.com/daveho/AceGWT/tarball/master --output-document=- 2> /dev/null ) \
	| tar xzf - --wildcards --strip-components=3 'daveho-AceGWT-*/AceGWT/src/edu/*' )
echo "done"


