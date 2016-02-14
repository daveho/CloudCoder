# Docker image for running CloudCoder and its dependencies

FROM ubuntu:trusty
MAINTAINER David Hovemeyer <david.hovemeyer@gmail.com>

# Run from the root user's home directory
WORKDIR /root

# Use the bootstrap script to install software and configure
# stuff.  A generic set of configuration properties is used
# noninteractively.
ADD bootstrap.pl .
ADD dockerconfig.properties .

#RUN ./bootstrap.pl --disable=apache --config=dockerconfig.properties
