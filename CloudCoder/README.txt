About CloudCoder
================

FIXME: this is out of date and needs to be rewritten!

CloudCoder is a web application for short programming exercises
for introductory computer science courses.

TODO: more information about WTF CloudCoder is meant to do.

TODO: information about how to register students, write problems,
analyze data, etc.

Installation
============

TODO: make the installation process more friendly.

Start by checking out CloudCoder from github:

  git clone https://github.com/daveho/CloudCoder.git

This will create a "CloudCoder" directory with four subdirectories:

  CloudCoder
  CloudCoderImporter
  CloudCoderBuilder
  CloudCoderWebServer

Make sure all four projects are extracted in
the same place.  (This will happen automatically if you cloned the
CloudCoder git repository as described above.)  The directory
containing these subdirectories will be referred to as $BASE.

Note: it is likely that you will want to host the web application
on one server, and host the "builder" (the component that
compiles and tests student submissions) on another
servers.  You should check out the code on both servers.

Step 1 - Configuring and Building
---------------------------------

Configuring and installing the webapp (on the server where
you will be hosting the webapp):

  cd $BASE
  ./configure.pl -webapp
  ./build_webapp.pl

[Note that the above will prompt you for the location of the
GWT SDK - the directory containing the "webAppCreator" script.
You can use the SDK in your Eclipse installation, or you can
use a standalone GWT SDK.  It must be version 2.4.0 or later.]

Configuring and installing the builder (on the server where you
will be hosting the builder):

  cd $BASE
  ./configure.pl -builder
  cd CloudCoderBuilder
  ant build

Note: it is highly recommended that you create a new user
account for the builder.  Make sure that this user does not
have access to any sensitive information (ssh keys, etc.)
Because the Builder executes untrusted code, it is possible
that files in the user account in which the builder is
run could be accessed.

Step 2 - Start the web app
--------------------------

On the webapp server:

  cd $BASE/CloudCoderWebServer
  ./start.sh

This starts a Jetty web server hosting the CloudCoder web application.
It will listen on port 8081.

TODO: allow use of SSL.

Step 3 - Start the builder
--------------------------

On the builder server:

  cd $BASE/CloudCoderBuilder
  ./start.sh

Step 4 - Profit
---------------

At this point you should be able to point your web browser at

  http://$WEBSERVER:8080

and use the running CloudCoder application.
