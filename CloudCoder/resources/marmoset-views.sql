-- expose marmoset students table as the CloudCoder cc_users table

create view cc_users as
  select students.student_pk as id,
         students.login_name as username,
         MD5(CONCAT(X'64c51d57faf2954c',students.password)) as password_md5,
         '64c51d57faf2954c' as salt
         from students;

-- expose marmoset courses table as CloudCoder cc_courses table
-- Assumes semester values are in the form 'Fall 2011', 'Spring 2012', etc.
create view cc_courses as
  select courses.course_pk as id,
         courses.coursename as name,
         courses.description as title,
         courses.url as url,
         CASE substring(courses.semester, 1, locate(' ', courses.semester) - 1)
           when 'Fall' then 6
           when 'Winter' then 1
           when 'Spring' then 2
           when 'Summer' then 3
           else 2 END as term_id,
         convert(substring(courses.semester from locate(' ', courses.semester) + 1), signed) as year
   from courses;

-- expose marmoset student_registration table as CloudCoder cc_course_registrations table
create view cc_course_registrations as
  select sr.student_registration_pk as id,
         sr.course_pk as course_id,
         sr.student_pk as user_id,
         CASE sr.instructor_capability
           when 'modify' then 1
           else 0 END as registration_type,
         CONVERT(sr.section, signed) as section
    from student_registration as sr;
