-- phpMyAdmin SQL Dump
-- version 3.4.5deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Feb 04, 2012 at 07:58 AM
-- Server version: 5.1.58
-- PHP Version: 5.3.6-13ubuntu3.3

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
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `event_id` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `start_row` mediumint(9) NOT NULL,
  `end_row` mediumint(9) NOT NULL,
  `start_col` mediumint(9) NOT NULL,
  `end_col` mediumint(9) NOT NULL,
  `text` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `event_id` (`event_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=174 ;

-- --------------------------------------------------------

--
-- Table structure for table `cc_configuration_settings`
--

CREATE TABLE IF NOT EXISTS `cc_configuration_settings` (
  `name` varchar(60) NOT NULL,
  `value` text NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

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
  `name` varchar(20) NOT NULL,
  `title` varchar(100) NOT NULL,
  `url` varchar(120) NOT NULL,
  `term_id` int(11) NOT NULL,
  `year` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

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
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=12 ;

--
-- Dumping data for table `cc_course_registrations`
--

INSERT INTO `cc_course_registrations` (`id`, `course_id`, `user_id`, `registration_type`, `section`) VALUES
(5, 1, 2, 0, 101),
(6, 1, 3, 0, 101),
(7, 1, 4, 0, 101),
(8, 1, 5, 0, 101),
(9, 1, 6, 0, 101);

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
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=189 ;

-- --------------------------------------------------------

--
-- Table structure for table `cc_problems`
--

CREATE TABLE IF NOT EXISTS `cc_problems` (
  `problem_id` int(11) NOT NULL AUTO_INCREMENT,
  `course_id` int(11) NOT NULL,
  `problem_type` int(11) NOT NULL,
  `testname` varchar(255) NOT NULL,
  `brief_description` varchar(60) NOT NULL,
  `description` varchar(8192) NOT NULL,
  `when_assigned` bigint(20) NOT NULL,
  `when_due` bigint(20) NOT NULL,
  `skeleton` varchar(400) DEFAULT NULL,
  PRIMARY KEY (`problem_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=12 ;

--
-- Dumping data for table `cc_problems`
--

INSERT INTO `cc_problems` (`problem_id`, `course_id`, `problem_type`, `testname`, `brief_description`, `description`, `when_assigned`, `when_due`, `skeleton`) VALUES
(1, 1, 0, 'sq', 'Square a number', 'Write a method called "sq" that returns the square of an integer parameter.', 0, 0, NULL),
(2, 1, 3, 'addInts', 'read/add/print sum of integers', '<p>Write a program which:</p>\n\n<ul>\n<li> Reads two integer values\n<li> Computes the sum of the integer values\n<li> Prints a line of the form "answer: <i>X</i>",\n     where <i>X</i> is the sum of the integer values\n</ul>\n\n<p>\nAny lines of text that are not of the form\n"answer: <i>X</i>" will be ignored.  If the program\nproduces more than 20 lines of output, it will terminate\nand the test will fail.\n</p>', 1326485534072, 1326571934072, '#include <stdio.h>\n\nint main(void) {\n	// TODO: add your code\n\n	return 0;\n}'),
(10, 1, 3, 'prob_6_1', 'Print integers from 1 to n', '\n<p>\nWrite a program that reads an integer (which you may assume\nwill be positive) and prints all of the integers from\n1 to that integer, <em>on a single line</em>.\n</p>\n\n<p>\nFor example, if the integer read by the program is <b>7</b>,\nthen the program should print a line reading\n</p>\n\n<blockquote>\n<pre>\n1 2 3 4 5 6 7\n</pre>\n</blockquote>\n\n<p>\nHint: make sure that there is at least one space between each\nnumber.  In other words, if the integer is <b>4</b>,\nthen the output should be <code>1 2 3 4</code>, not <code>1234</code>.\n</p>\n\n<p>\nIf the program produces more than 20 lines of output, it will terminate and the test will fail.\n</p>\n\n	', 1328191200000, 1328623200000, '#include <stdio.h>\n\nint main(void) {\n	// TODO: your code goes here\n\n	return 0;\n}\n\n	'),
(11, 1, 3, 'prob_6_2', 'Blast off!', '\n<p>\nWrite a program that reads an integer (which you may assume\nwill be positive) and prints a single line of the form\n</p>\n\n<blockquote>\n<pre>\n5...4...3...2...1...blast off!\n</pre>\n</blockquote>\n\n<p>\nwhere the countdown starts at the integer read, and continues\ndown to 1.  For example, the output above should be printed\nif the input integer is <b>5</b>.\n</p>\n\n<p>\nHint: make sure that each number printed is followed by exactly\nthree period (".") characters, and that <b>blast off!</b>\nis printed at the end of the line.  Aside from the space\nbetween "blast" and "off", the line should not contain\nany spaces.\n</p>\n\n<p>\nIf the program produces more than 20 lines of output, it will terminate and the test will fail.\n</p>\n\n	', 1328191200000, 1328623200000, '#include <stdio.h>\n\nint main(void) {\n	// TODO: your code goes here\n\n	return 0;\n}\n\n	');

-- --------------------------------------------------------

--
-- Table structure for table `cc_submission_receipts`
--

CREATE TABLE IF NOT EXISTS `cc_submission_receipts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `event_id` int(11) NOT NULL,
  `last_edit_event_id` int(11) NOT NULL,
  `status` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `event_id` (`event_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=16 ;

-- --------------------------------------------------------

--
-- Table structure for table `cc_terms`
--

CREATE TABLE IF NOT EXISTS `cc_terms` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL,
  `seq` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=7 ;

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
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=49 ;

--
-- Dumping data for table `cc_test_cases`
--

INSERT INTO `cc_test_cases` (`test_case_id`, `problem_id`, `test_case_name`, `input`, `output`, `secret`) VALUES
(2, 1, 'test1', '5', '25', 0),
(3, 1, 'test2', '-1', '1', 0),
(4, 1, 'test3', '9', '81', 0),
(5, 1, 'test4', '10', '100', 0),
(6, 2, 'fourPlusFive', '4 5', '^\\s*answer\\s*:\\s*(0*)9\\s*$', 0),
(7, 2, 'sixPlusSeven', '6 7', '^\\s*answer\\s*:\\s*(0*)13\\s*$', 0),
(8, 2, 'threePlusNegSeventeen', '3 -17', '^\\s*answer\\s*:\\s*-(0*)14\\s*$', 0),
(9, 2, 'onePlusZero', '1 0', '^\\s*answer\\s*:\\s*(0*)1\\s*$', 1),
(10, 2, 'negOnePlusNeg2', '-1 -2', '^\\s*answer\\s*:\\s*-(0*)3\\s*$', 1),
(11, 3, 'nEquals7', '7', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*6\\s*7\\s*$', 0),
(12, 3, 'nEquals4', '4', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*$', 0),
(13, 3, 'nEquals5', '5', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*$', 0),
(14, 3, 'nEquals9', '9', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*6\\s*7\\s*8\\s*9\\s*$', 1),
(15, 4, 'nEquals7', '7', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*6\\s*7\\s*$', 0),
(16, 4, 'nEquals4', '4', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*$', 0),
(17, 4, 'nEquals5', '5', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*$', 0),
(18, 4, 'nEquals9', '9', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*6\\s*7\\s*8\\s*9\\s*$', 1),
(19, 5, 'nEquals7', '7', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*6\\s*7\\s*$', 0),
(20, 5, 'nEquals4', '4', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*$', 0),
(21, 5, 'nEquals5', '5', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*$', 0),
(22, 5, 'nEquals9', '9', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*6\\s*7\\s*8\\s*9\\s*$', 1),
(23, 6, 'nEquals7', '7', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*6\\s*7\\s*$', 0),
(24, 6, 'nEquals4', '4', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*$', 0),
(25, 6, 'nEquals5', '5', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*$', 0),
(26, 6, 'nEquals9', '9', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*6\\s*7\\s*8\\s*9\\s*$', 1),
(27, 7, 'from3', '3', '^\\s*\\Q3...2...1...\\Eblast\\s*off!\\s*$', 0),
(28, 7, 'from5', '5', '^\\s*\\Q5...4...3...2...1...\\Eblast\\s*off!\\s*$', 0),
(29, 7, 'from10', '5', '^\\s*\\Q10...9...8...7...6...5...4...3...2...1...\\Eblast\\s*off!\\s*$', 0),
(30, 7, 'from7', '9', '^\\s*\\Q7...6...5...4...3...2...1...\\Eblast\\s*off!\\s*$', 1),
(31, 8, 'from3', '3', '^\\s*\\Q3...2...1...\\Eblast\\s*off!\\s*$', 0),
(32, 8, 'from5', '5', '^\\s*\\Q5...4...3...2...1...\\Eblast\\s*off!\\s*$', 0),
(33, 8, 'from10', '5', '^\\s*\\Q10...9...8...7...6...5...4...3...2...1...\\Eblast\\s*off!\\s*$', 0),
(34, 8, 'from7', '9', '^\\s*\\Q7...6...5...4...3...2...1...\\Eblast\\s*off!\\s*$', 1),
(35, 9, 'from3', '3', '^\\s*\\Q3...2...1...\\Eblast\\s*off!\\s*$', 0),
(36, 9, 'from5', '5', '^\\s*\\Q5...4...3...2...1...\\Eblast\\s*off!\\s*$', 0),
(37, 9, 'from10', '10', '^\\s*\\Q10...9...8...7...6...5...4...3...2...1...\\Eblast\\s*off!\\s*$', 0),
(38, 9, 'from7', '9', '^\\s*\\Q9...8...7...6...5...4...3...2...1...\\Eblast\\s*off!\\s*$', 1),
(39, 10, 'nEquals7', '7', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*6\\s*7\\s*$', 0),
(40, 10, 'nEquals4', '4', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*$', 0),
(41, 10, 'nEquals5', '5', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*$', 0),
(42, 10, 'nEquals9', '9', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*6\\s*7\\s*8\\s*9\\s*$', 1),
(43, 11, 'from3', '3', '^\\s*\\Q3...2...1...\\Eblast\\s*off!\\s*$', 0),
(44, 11, 'from5', '5', '^\\s*\\Q5...4...3...2...1...\\Eblast\\s*off!\\s*$', 0),
(45, 11, 'from10', '10', '^\\s*\\Q10...9...8...7...6...5...4...3...2...1...\\Eblast\\s*off!\\s*$', 0),
(46, 11, 'from7', '9', '^\\s*\\Q9...8...7...6...5...4...3...2...1...\\Eblast\\s*off!\\s*$', 1),
(47, 2, 'test6', '39 22', '^\\s*answer\\s*:\\s*(0+)?61\\s*$', 1),
(48, 2, 'test7', '-14 29', '^\\s*answer\\s*:\\s*(0+)?15\\s*$', 1);

-- --------------------------------------------------------

--
-- Table structure for table `cc_test_results`
--

CREATE TABLE IF NOT EXISTS `cc_test_results` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `submission_receipt_id` int(11) NOT NULL,
  `test_outcome` int(11) NOT NULL,
  `message` varchar(100) NOT NULL,
  `stdout` text NOT NULL,
  `stderr` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `submission_receipt_id` (`submission_receipt_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=77 ;

-- --------------------------------------------------------

--
-- Table structure for table `cc_users`
--

CREATE TABLE IF NOT EXISTS `cc_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(20) CHARACTER SET utf8 DEFAULT NULL,
  `password_md5` varchar(32) CHARACTER SET utf8 DEFAULT NULL,
  `salt` varchar(16) CHARACTER SET utf8 DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `users_username_index` (`username`)
) ENGINE=MyISAM  DEFAULT CHARSET=ucs2 AUTO_INCREMENT=9 ;

--
-- Dumping data for table `cc_users`
--

INSERT INTO `cc_users` (`id`, `username`, `password_md5`, `salt`) VALUES
(2, 'dbabcock', 'a790a0c6ff0f9f89c482e827eb6a2873', '64c51d57faf2954c'),
(3, 'jmoscola', 'a790a0c6ff0f9f89c482e827eb6a2873', '64c51d57faf2954c'),
(4, 'dhovemey', 'a790a0c6ff0f9f89c482e827eb6a2873', '64c51d57faf2954c'),
(5, 'mmmiller', 'a790a0c6ff0f9f89c482e827eb6a2873', '64c51d57faf2954c'),
(6, 'jspacco', 'a790a0c6ff0f9f89c482e827eb6a2873', '64c51d57faf2954c');

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
