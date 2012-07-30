-- phpMyAdmin SQL Dump
-- version 3.4.10.1deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jul 30, 2012 at 10:10 AM
-- Server version: 5.5.24
-- PHP Version: 5.3.10-1ubuntu3.2

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `cloudcoder`
--

-- --------------------------------------------------------

--
-- Table structure for table `cc_changes`
--

CREATE TABLE IF NOT EXISTS `cc_changes` (
  `event_id` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `start_row` mediumint(9) NOT NULL,
  `end_row` mediumint(9) NOT NULL,
  `start_col` mediumint(9) NOT NULL,
  `end_col` mediumint(9) NOT NULL,
  `text_short` varchar(80) DEFAULT NULL,
  `text` text,
  UNIQUE KEY `event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `cc_configuration_settings`
--

CREATE TABLE IF NOT EXISTS `cc_configuration_settings` (
  `name` varchar(60) NOT NULL,
  `value` text NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `cc_configuration_settings`
--

INSERT INTO `cc_configuration_settings` (`name`, `value`) VALUES
('pub.text.institution', 'York College of Pennsylvania');

-- --------------------------------------------------------

--
-- Table structure for table `cc_courses`
--

CREATE TABLE IF NOT EXISTS `cc_courses` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) CHARACTER SET latin1 NOT NULL,
  `title` varchar(100) CHARACTER SET latin1 NOT NULL,
  `url` varchar(120) CHARACTER SET latin1 NOT NULL,
  `term_id` int(11) NOT NULL,
  `year` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `cc_courses`
--

INSERT INTO `cc_courses` (`id`, `name`, `title`, `url`, `term_id`, `year`) VALUES
(1, 'CS 101', 'Introduction to Computer Science I', 'http://cs.unseen.edu/s11/cs101', 1, 2011);

-- --------------------------------------------------------

--
-- Table structure for table `cc_course_registrations`
--

CREATE TABLE IF NOT EXISTS `cc_course_registrations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `course_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `registration_type` int(11) NOT NULL,
  `section` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=10 ;

--
-- Dumping data for table `cc_course_registrations`
--

INSERT INTO `cc_course_registrations` (`id`, `course_id`, `user_id`, `registration_type`, `section`) VALUES
(5, 1, 2, 1, 101),
(6, 1, 3, 1, 101),
(7, 1, 4, 1, 101),
(8, 1, 5, 1, 101),
(9, 1, 6, 1, 101);

-- --------------------------------------------------------

--
-- Table structure for table `cc_events`
--

CREATE TABLE IF NOT EXISTS `cc_events` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `problem_id` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `timestamp` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `problem_id` (`problem_id`),
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `cc_problems`
--

CREATE TABLE IF NOT EXISTS `cc_problems` (
  `problem_id` int(11) NOT NULL AUTO_INCREMENT,
  `course_id` int(11) NOT NULL,
  `when_assigned` bigint(20) NOT NULL,
  `when_due` bigint(20) NOT NULL,
  `visible` tinyint(1) NOT NULL DEFAULT '0',
  `problem_type` int(11) NOT NULL,
  `testname` varchar(255) NOT NULL,
  `brief_description` varchar(60) NOT NULL,
  `description` varchar(8192) NOT NULL,
  `skeleton` varchar(400) DEFAULT NULL,
  `schema_version` int(11) NOT NULL DEFAULT '0',
  `author_name` varchar(80) NOT NULL DEFAULT '',
  `author_email` varchar(80) NOT NULL DEFAULT '',
  `author_website` varchar(100) NOT NULL DEFAULT '',
  `timestamp_utc` bigint(20) NOT NULL DEFAULT '0',
  `license` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`problem_id`),
  KEY `author_name` (`author_name`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=4 ;

--
-- Dumping data for table `cc_problems`
--

INSERT INTO `cc_problems` (`problem_id`, `course_id`, `when_assigned`, `when_due`, `visible`, `problem_type`, `testname`, `brief_description`, `description`, `skeleton`, `schema_version`, `author_name`, `author_email`, `author_website`, `timestamp_utc`, `license`) VALUES
(2, 1, 0, 0, 0, 3, 'prob_6_1', 'Print integers from 1 to n', '<p>\nWrite a program that reads an integer (which you may assume\nwill be positive) and prints all of the integers from\n1 to that integer, <em>on a single line</em>.\n</p>\n\n<p>\nFor example, if the integer read by the program is <b>7</b>,\nthen the program should print a line reading\n</p>\n\n<blockquote>\n<pre>\n1 2 3 4 5 6 7\n</pre>\n</blockquote>\n\n<p>\nHint: make sure that there is at least one space between each\nnumber.  In other words, if the integer is <b>4</b>,\nthen the output should be <code>1 2 3 4</code>, not <code>1234</code>.\n</p>\n\n<p>\nIf the program produces more than 20 lines of output, it will terminate and the test will fail.\n</p>\n\n	', '#include <stdio.h>\n\nint main(void) {\n	// TODO: your code goes here\n\n	return 0;\n}\n\n	', 0, '', '', '', 0, 0),
(3, 1, 1343689200000, 1343777640000, 0, 3, 'hello', 'Print hello world', '<p>Print a line with the text:</p>\n<blockquote>\n<pre>Hello, world</pre>\n</blockquote>\n', '#include <stdio.h>\n\nint main(void) {\n    // TODO: add your code here\n    \n    return 0;\n}', 0, 'David Hovemeyer', 'dhovemey@ycp.edu', 'http://faculty.ycp.edu/~dhovemey/', 1343664000000, 0);

-- --------------------------------------------------------

--
-- Table structure for table `cc_submission_receipts`
--

CREATE TABLE IF NOT EXISTS `cc_submission_receipts` (
  `event_id` int(11) NOT NULL,
  `last_edit_event_id` int(11) NOT NULL,
  `status` int(11) NOT NULL,
  `num_tests_attempted` int(11) NOT NULL,
  `num_tests_passed` int(11) NOT NULL,
  UNIQUE KEY `event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `cc_terms`
--

CREATE TABLE IF NOT EXISTS `cc_terms` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL,
  `seq` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=7 ;

--
-- Dumping data for table `cc_terms`
--

INSERT INTO `cc_terms` (`id`, `name`, `seq`) VALUES
(1, 'Winter', 1),
(2, 'Spring', 2),
(3, 'Summer', 3),
(4, 'Summer I', 4),
(5, 'Summer II', 5),
(6, 'Fall', 6);

-- --------------------------------------------------------

--
-- Table structure for table `cc_test_cases`
--

CREATE TABLE IF NOT EXISTS `cc_test_cases` (
  `test_case_id` int(11) NOT NULL AUTO_INCREMENT,
  `problem_id` int(11) NOT NULL,
  `test_case_name` varchar(40) DEFAULT NULL,
  `input` varchar(255) NOT NULL,
  `output` varchar(255) NOT NULL,
  `secret` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`test_case_id`),
  KEY `problem_id` (`problem_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=11 ;

--
-- Dumping data for table `cc_test_cases`
--

INSERT INTO `cc_test_cases` (`test_case_id`, `problem_id`, `test_case_name`, `input`, `output`, `secret`) VALUES
(1, 2, 'nEquals7', '7', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*6\\s*7\\s*$', 0),
(2, 2, 'nEquals4', '4', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*$', 0),
(3, 2, 'nEquals5', '5', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*$', 0),
(4, 2, 'nEquals9', '9', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*6\\s*7\\s*8\\s*9\\s*$', 1),
(10, 3, 'hello', '', '^\\s*Hello,\\s*world\\s*$i', 0);

-- --------------------------------------------------------

--
-- Table structure for table `cc_test_results`
--

CREATE TABLE IF NOT EXISTS `cc_test_results` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `submission_receipt_event_id` int(11) NOT NULL,
  `test_outcome` int(11) NOT NULL,
  `message` varchar(100) NOT NULL,
  `stdout` text NOT NULL,
  `stderr` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `submission_receipt_id` (`submission_receipt_event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `cc_users`
--

CREATE TABLE IF NOT EXISTS `cc_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(20) DEFAULT NULL,
  `password_hash` varchar(60) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `users_username_index` (`username`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=7 ;

--
-- Dumping data for table `cc_users`
--

INSERT INTO `cc_users` (`id`, `username`, `password_hash`) VALUES
(2, 'dbabcock', '$2a$12$LtIWP6JdZT3w0dz9kdTlXusKrG0QrfyIptl.4w3bc9YCrAm1.j3Mi'),
(3, 'jmoscola', '$2a$12$Sg7.hnSIzrl3UYjISCH.EuAeTuuhEesWqr4UzJqAe9wjwri//CGfW'),
(4, 'dhovemey', '$2a$12$H0We7TjqAP8rkRiTbwuzyuwDHKgtFWRhxZBgwUKF0evipRcmO.Lga'),
(5, 'mmmiller', '$2a$12$ZzluQmpcTCQOkT6gU/ve3ugKhK8YKPtryjocqrpaCM2phRuPidfi.'),
(6, 'jspacco', '$2a$12$E2clZcNDMSSA.5gkZ3bwl.QBKXWgoF2NNV2RLOn29gmzoDv531Pj.');

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
