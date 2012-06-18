-- phpMyAdmin SQL Dump
-- version 3.4.10.1deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jun 18, 2012 at 10:48 AM
-- Server version: 5.5.24
-- PHP Version: 5.3.10-1ubuntu3.1

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
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=5 ;

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
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=12 ;

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
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=6 ;

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
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=2 ;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

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
  `password_md5` varchar(32) DEFAULT NULL,
  `salt` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `users_username_index` (`username`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=9 ;

--
-- Dumping data for table `cc_users`
--

INSERT INTO `cc_users` (`id`, `username`, `password_md5`, `salt`) VALUES
(2, 'dbabcock', 'a790a0c6ff0f9f89c482e827eb6a2873', '64c51d57faf2954c'),
(3, 'jmoscola', 'a790a0c6ff0f9f89c482e827eb6a2873', '64c51d57faf2954c'),
(4, 'dhovemey', '215744769073444d231694edbc2e65f6', '1cce968f060d491a'),
(5, 'mmmiller', 'a790a0c6ff0f9f89c482e827eb6a2873', '64c51d57faf2954c'),
(6, 'jspacco', 'a790a0c6ff0f9f89c482e827eb6a2873', '64c51d57faf2954c');

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
