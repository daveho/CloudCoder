#! /bin/bash

# Run the CloudCoder webapp in GWT Super Dev Mode!
#
# As of today (May 26, 2014) classic dev mode does not seem
# to be supported on any current browser.  So, we need to
# use Super Dev Mode:
#
#   http://www.gwtproject.org/articles/superdevmode.html
#
# Here's how to use this script:
#
#   1. Run this script in a terminal window.  It will run the
#      GWT code server as a foreground process.
#
#   2. Assuming everything compiled correctly, you should see
#      the following messages:
#           The code server is ready.
#           Next, visit: http://localhost:9876/
#      Visit the link in your web browser.  Create the bookmarks
#      as indicated if you have not already done so.  Then click
#      on the cloudcoder module link.
#
#   2. In Eclipse, right-click CloudCoderSuperDevMode.gwt.xml
#      (in the org.cloudcoder.app package), and choose
#      Run As->Web Application.
#
#   3. Copy the development mode URL.  Paste it into your web
#      browser, but omit the part of the URL that ends
#      "gwt.codesvr=127.0.0.1:9997".  Most likely, the URL
#      should be "http://127.0.0.1:8888/CloudCoder.html".
#      Load this in your browser.  You will get a message about
#      the module possibly needing to be recompiled.
#      Click the "Dev Mode On" bookmark.  Recompile the
#      cloudcoder module.  The recompilation dialog will appear
#      again (don't know why, seems wrong).  At this point
#      CloudCoder should be running in super dev mode.
#
#    4. Use control-C in the console window where you started
#       this script to shut down the code server  Also, shut down the
#       webapp in Eclipse (i.e., by clicking the red button
#       in the Console or Development Mode views.)
#
# I have only tested this with GWT 2.6.0.

# Transform a list of items by prepending a prefix and appending a
# suffix to each item.
pp() {
	list="$1"
	pfx="$2"
	sfx="$3"
	for item in $list; do
		echo -n "${pfx}${item}${sfx}"
	done
}

# The GNU/Linux mktemp program has different options than
# the BSD/MacOS mktemp program.
my_mktemp() {
	osname=$(uname)
	if [ "$osname" = "Linux" ]; then
		mktemp -d
	else
		mktemp -d -tccsdev
	fi
}

# Read cloudcoder.properties to find where the GWT SDK is located.
gwt_dir=$(egrep '^gwt\.sdk' ../cloudcoder.properties | cut -d= -f 2)

# GWT jarfiles that should be on the classpath
gwt_libs="gwt-codeserver gwt-user gwt-dev validation-api-1.0.0.GA"

# GWT source jarfiles that should be on the source classpath
gwt_srclibs="validation-api-1.0.0.GA-sources"

# CloudCoder projects that should be on the classpath (as source)
cc_srcprojs="CloudCoderModelClasses"

# Jars containing required GWT modules (as source)
cc_srclibs="gwt-traction-1.5.8"

bincp1=$(pp "$gwt_libs" "${gwt_dir}/" ".jar:")
srccp1=$(pp "$cc_srcprojs" " -src ../" "/src ")

# The GWT CodeServer doesn't seem to be able to find sources in
# a jar file (which is stupid, since gwtc most definitely CAN
# read sources from a jarfile.)  So, create a temporary directory
# for each source jar.
srccp2=''
tmpdirs=''
allsrcjars="$(pp "$cc_srclibs" "srclib/" " ") $(pp "$gwt_srclibs" "${gwt_dir}/" " ")"
#echo allsrcjars=$allsrcjars
for j in $allsrcjars; do
	td=$(my_mktemp)
	mkdir -p $td
	echo "Extracting source jar ${j}.jar into ${td}..."
	cat ${j}.jar | (cd $td && jar x)
	srccp2="$srccp2 -src $td"
	tmpdirs="$tmpdirs $td"
done

cmd="java -classpath ${bincp1} -Xmx1024m com.google.gwt.dev.codeserver.CodeServer -src src/ $srccp1 $srccp2 org.cloudcoder.app.CloudCoderSuperDevMode"

echo "cmd=${cmd}"

# Start the code server!
# It runs in the foreground, with the assumption that it will
# be shut down with control-C.
$cmd

# Cleanup $tmpdirs
for td in $tmpdirs; do
	echo "Deleting temp directory $td..."
	rm -rf $td
done
