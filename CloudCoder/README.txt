About CloudCoder
================

CloudCoder is a web application for short programming exercises
for introductory computer science courses.

TODO: more information about WTF CloudCoder is meant to do.

TODO: information about how to register students, write problems,
analyze data, etc.

Installation
============

If you want 

TODO: make the installation process more friendly.

There are three Eclipse projects:
  CloudCoder
  CloudCoderOutOfProcessSubmitService
  CloudCoderWebServer

Import all of the projects into Eclipse.  Make sure all
three working directories are extracted in the same place.
(This will happen automatically if you cloned the
CloudCoder git repository.)  This directory will be
referred to as $BASE.

Step 1 - Building Projects
--------------------------

Build the CloudCoder project using the Google toolbar item,
choosing the "GWT Compile Project..." menu item.  (You will
need the Google Eclipse Plugin and GWT version 2.4.0 or later.)

The other two projects will be built automatically.

Step 2 - Copy compiled web app to web server project
----------------------------------------------------

From the command line:

  cd $BASE/CloudCoder
  ./copy_webapp.sh

This will copy the compiled web application into the "apps"
subdirectory of CloudCoderWebServer.

Step 3 - Start the web app
--------------------------

From the command line

  cd $BASE/CloudCoderWebServer
  ./start.sh

This starts a Jetty web server hosting the CloudCoder web application.
It will listen on port 8080.

TODO: allow use of SSL.

Step 4 - Start the builder
--------------------------

The builder (a server which builds and tests submissions) can run
on a different host as the web server which hosts the web application.
Also, many builders may be used with a single web application.
This is encouraged: in order to minimize the latency between the
student submitting their work and the system responding with tests
results, as many builders as necessary should be deployed.
We'll refer to the hostname of the webserver hosting the web app
as $WEBSERVER.

By default, the port 47374 is used to allow Builders to connect to
the web application.

From the command line:

  cd $BASE/CloudCoderOutOfProcessSubmitService
  ./start.sh $WEBSERVER

This will start a builder connected to the web app running on
$WEBSERVER.

Step 5 - Profit
---------------

At this point you should be able to point your web browser at

  http://$WEBSERVER:8080

and use the running CloudCoder application.
  