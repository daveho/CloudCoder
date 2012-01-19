#! /bin/bash

dbname=cloudcoder

dbuser=root

# This may fail if the database does not currently exist
echo "drop database $dbname;" \
	| mysql --user=$dbuser --pass

(echo "create database $dbname; use $dbname; " && cat cloudcoder-schema.sql) \
	| sed 's,InnoDB,MyISAM,' \
	| mysql --user=$dbuser --pass
