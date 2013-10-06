#! /bin/bash

# Strip off the name of the program to run.
prog="$1"
shift

#
# Execute the wrapped program in a subprocess shell,
# setting resource limits as indicated by the 
# CC_PROCESS_RESOURCE_LIMITS env var.
# Also, if the CC_LD_PRELOAD environment variable is set,
# set LD_PRELOAD to its value.  (This is used for sandboxing
# using the EasySandbox shared library.)
# And, if set, use the CC_EASYSANDBOX_HEAPSIZE to set the EASYSANDBOX_HEAPSIZE
# environment variable (to set a heap size for a sandboxed process).
#
# Note that the program is run asynchronously in the background,
# so that if the parent process (i.e., the CloudCoder builder)
# needs to kill the program, it can send a signal to this script
# which in turn can kill the actual program.
#

# If this script is sent a SIGTERM signal, then pass it on to the
# program process.
program_pid=""
on_sigterm_received() {
	if [ ! -z "${program_pid}" ]; then
		kill -TERM ${program_pid}
	fi
}
trap "on_sigterm_received" SIGTERM

# Execute the actual program, creating a background process.
(for limit in ${CC_PROCESS_RESOURCE_LIMITS}; do ulimit $limit; done; \
if [ ! -z "${CC_LD_PRELOAD}" ]; then export LD_PRELOAD=${CC_LD_PRELOAD}; fi; \
if [ ! -z "${CC_EASYSANDBOX_HEAPSIZE}" ]; then export EASYSANDBOX_HEAPSIZE=${CC_EASYSANDBOX_HEAPSIZE}; fi; \
exec ${prog} "$@")&
program_pid=$!

# Wait (synchronously) for the program to exit
wait $program_pid
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
