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

# Important: the database must already exist

(echo "use $dbname; " && cat cloudcoder-schema.sql) \
	| sed 's,InnoDB,MyISAM,' \
	| mysql --user=$dbuser --pass
