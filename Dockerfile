# Docker image for running CloudCoder, MySQL, and Apache.
# Run like this:
#
#   docker run -p 443:443 -p 47374:47374 cloudcoder-mysql-apache2
#
# Note that this image will use a self-signed "snakeoil"
# certificate (regenerated automatically when the image
# starts for the first time.)  You should configure a
# "real" SSL certificate before running the image in
# production.
#
# NOTE: At the current time I'm not sure Docker is the best
# approach for deploying CloudCoder, so even though the
# Docker image kind of works (and it was fun playing around
# with Docker), I'm not going to work on it any more for
# the time being.

FROM ubuntu:trusty
MAINTAINER David Hovemeyer <david.hovemeyer@gmail.com>

# Webapp will listen for HTTPS connections on port 443,
# and builder connections on port 47374.
EXPOSE 443 47374

# Run from the root user's home directory
WORKDIR /root

# Use the bootstrap script to install software and configure
# stuff.  A generic set of configuration properties is used
# noninteractively.
ADD bootstrap.pl .
ADD dockerconfig.properties .
ADD dockerrun.pl .
RUN ./bootstrap.pl \
  --enable=integrated-builder \
  --config=dockerconfig.properties \
  --no-start \
  --defer-keystore

# The dockerrun.pl script starts mysql and the CloudCoder webapp,
# and shuts them down cleanly when a SIGTERM is received.
ENTRYPOINT ["./dockerrun.pl"]
