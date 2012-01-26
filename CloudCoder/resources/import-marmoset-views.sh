#! /bin/bash

dbname=cloudcoder
dbuser=marmoset

if [ $# -ge 1 ]; then
	dbname="$1"
fi
if [ $# -ge 2 ]; then
	dbuser=marmoset
fi

echo "dbname=$dbname"
echo "dbuser=$dbuser"

(echo "use $dbname; " &&
 echo "drop table cc_users; drop table cc_courses; drop table cc_course_registrations; commit; " && \
 cat ./marmoset-views.sql) | mysql --user=$dbuser --pass
 