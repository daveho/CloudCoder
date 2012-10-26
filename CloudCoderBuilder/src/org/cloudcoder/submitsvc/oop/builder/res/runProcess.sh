#! /bin/bash

# Strip off the name of the program to run.
prog="$1"
shift

# Execute the wrapped program in a subprocess shell,
# setting resource limits as indicated by the 
# CC_PROCESS_RESOURCE_LIMITS env var.
(for limit in ${CC_PROCESS_RESOURCE_LIMITS}; do ulimit $limit; done; exec ${prog} "$@")

rc=$?

if [ -z "${CC_PROC_STAT_FILE}" ]; then
	# A process status file was not requested.
	exit $rc
fi

rm -f ${CC_PROC_STAT_FILE}

if [ $rc -eq 127 ]; then
	# Program could not be executed
	echo "failed_to_execute" > ${CC_PROC_STAT_FILE}
	echo "-1" >> ${CC_PROC_STAT_FILE}
	exit $rc
fi

if [ $rc -lt 128 ]; then
	# Normal process exit
	echo "exited" > ${CC_PROC_STAT_FILE}
	echo "$rc" >> ${CC_PROC_STAT_FILE}
	exit $rc
fi

# Process was terminated by a signal.
signo=`expr $rc - 128`
echo "terminated_by_signal" > ${CC_PROC_STAT_FILE}
echo "$signo" >> ${CC_PROC_STAT_FILE}
exit $rc
