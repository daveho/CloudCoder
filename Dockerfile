# Docker image for running CloudCoder and MySQL.
# The webapp will listen for unencrypted HTTP connections
# on port 8081.  So, you should run it something
# like the following:
#
#   docker run -d -p 8081:8081 -p 47374:47374 cloudcoder

FROM ubuntu:trusty
MAINTAINER David Hovemeyer <david.hovemeyer@gmail.com>

# Webapp will listen for HTTP connections on port 8081,
# and builder connections on port 47374.
EXPOSE 8081 47374

# Run from the root user's home directory
WORKDIR /root

# Use the bootstrap script to install software and configure
# stuff.  A generic set of configuration properties is used
# noninteractively.
ADD bootstrap.pl .
ADD dockerconfig.properties .
ADD dockerrun.pl .
RUN ./bootstrap.pl \
  --disable=apache \
  --config=dockerconfig.properties \
  --no-start \
  --no-localhost-only

# The dockerrun.pl script starts mysql and the CloudCoder webapp,
# and shuts them down cleanly when a SIGTERM is received.
ENTRYPOINT ["./dockerrun.pl"]
