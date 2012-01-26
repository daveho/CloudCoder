-- expose marmoset students table as the CloudCoder cc_users table

create view cc_users as
  select students.student_pk as id,
         students.login_name as username,
         MD5(CONCAT(X'64c51d57faf2954c',students.password)) as password_md5,
         '64c51d57faf2954c' as salt
         from students;

-- expose marmoset courses table as CloudCoder cc_courses table
create view cc_courses as
  select courses.course_pk as id,
         courses.coursename as name,
         courses.description as title,
         courses.url as url,
         2 as term,                  -- XXX: hardcoded term "Spring"
         2012 as year                -- XXX: hardcoded year
         from courses;

-- expose marmoset student_registration table as CloudCoder cc_course_registrations table
create view cc_course_registrations as
  select sr.student_registration_pk as id,
         sr.course_pk as course_id,
         sr.student_pk as user_id,
         0 as registration_type
         from student_registration as sr;

