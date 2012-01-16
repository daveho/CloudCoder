#! /bin/bash

# Shut down a CloudCoder server by sending the "quit"
# command to its FIFO and then waiting for the server
# process to exit.

app=cloudCoder

pid=`cat $app.pid`
if [ -z "$pid" ]; then
	echo "No $app.pid file"
	exit 1
fi

FIFO="$app-$pid.fifo"
if [ ! -e "$FIFO" ]; then
	echo "Could not find fifo $FIFO"
	exit 1
fi

echo "Sending quit command to server"
echo "quit" > $FIFO

echo -n "Waiting for server to finish..."

done="no"
while [ "$done" != "yes" ]; do
	/bin/ps -p $pid > /dev/null 2>&1
	rc=$?
	if [ "$rc" = "1" ]; then
		# Server process is gone
		done="yes"
	else
		sleep 1
	fi
done

echo "ok"

echo "Cleaning up pid file and fifo..."
rm $app.pid
rm $app-$pid.fifo
