-- a view that can be added to a marmoset database to
-- expose the students table as the CloudCoder cc_users table

create view cc_users as
  select students.student_pk as id,
         students.login_name as username,
         MD5(CONCAT(X'64c51d57faf2954c',students.password)) as password_md5,
         '64c51d57faf2954c' as salt
         from students;
