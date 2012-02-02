#! /bin/bash

# Wrapper for runProcess.pl that uses ulimit to set resource limits.

for limit in ${CC_PROCESS_RESOURCE_LIMITS}; do
	ulimit $limit
done

scriptdir=`dirname "$0"`
exec "$scriptdir/runProcess.pl" "$@"
