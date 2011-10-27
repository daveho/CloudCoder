-- phpMyAdmin SQL Dump
-- version 3.3.10deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Oct 27, 2011 at 12:13 PM
-- Server version: 5.1.54
-- PHP Version: 5.3.5-1ubuntu7.3

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `cloudcoder`
--

-- --------------------------------------------------------

--
-- Table structure for table `affect_events`
--

CREATE TABLE IF NOT EXISTS `affect_events` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `event_id` int(11) NOT NULL,
  `emotion` tinyint(4) NOT NULL,
  `other_emotion` varchar(40) DEFAULT NULL,
  `emotion_level` tinyint(4) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

--
-- Dumping data for table `affect_events`
--


-- --------------------------------------------------------

--
-- Table structure for table `changes`
--

CREATE TABLE IF NOT EXISTS `changes` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

--
-- Dumping data for table `changes`
--


-- --------------------------------------------------------

--
-- Table structure for table `configuration_settings`
--

CREATE TABLE IF NOT EXISTS `configuration_settings` (
  `name` varchar(60) NOT NULL,
  `value` text NOT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `configuration_settings`
--

INSERT INTO `configuration_settings` (`name`, `value`) VALUES
('pub.text.institution', 'York College of Pennsylvania');

-- --------------------------------------------------------

--
-- Table structure for table `courses`
--

CREATE TABLE IF NOT EXISTS `courses` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL,
  `title` varchar(100) NOT NULL,
  `url` varchar(120) NOT NULL,
  `term_id` int(11) NOT NULL,
  `year` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

--
-- Dumping data for table `courses`
--

INSERT INTO `courses` (`id`, `name`, `title`, `url`, `term_id`, `year`) VALUES
(1, 'CS 101', 'Introduction to Computer Science I', 'http://cs.unseen.edu/s11/cs101', 1, 2011),
(2, 'CS 201', 'Introduction to Computer Science II', 'http://cs.unseen.edu/f10/cs201', 5, 2010),
(3, 'CS 340', 'Programming Language Design', 'http://cs.unseen.edu/f10/cs340', 5, 2010),
(4, 'CS 350', 'Data Structures', 'http://cs.unseen.edu/s10/cs350', 1, 2010);

-- --------------------------------------------------------

--
-- Table structure for table `course_registrations`
--

CREATE TABLE IF NOT EXISTS `course_registrations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `course_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `registration_type` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

--
-- Dumping data for table `course_registrations`
--

INSERT INTO `course_registrations` (`id`, `course_id`, `user_id`, `registration_type`) VALUES
(1, 1, 1, 0),
(2, 2, 1, 0),
(3, 3, 1, 0),
(4, 4, 1, 0);

-- --------------------------------------------------------

--
-- Table structure for table `events`
--

CREATE TABLE IF NOT EXISTS `events` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `problem_id` int(11) NOT NULL,
  `type` int(11) NOT NULL,
  `timestamp` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `problem_id` (`problem_id`),
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

--
-- Dumping data for table `events`
--


-- --------------------------------------------------------

--
-- Table structure for table `problems`
--

CREATE TABLE IF NOT EXISTS `problems` (
  `problem_id` int(11) NOT NULL AUTO_INCREMENT,
  `course_id` int(11) NOT NULL,
  `testname` varchar(255) NOT NULL,
  `brief_description` varchar(60) NOT NULL,
  `description` varchar(255) NOT NULL,
  PRIMARY KEY (`problem_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `problems`
--

INSERT INTO `problems` (`problem_id`, `course_id`, `testname`, `brief_description`, `description`) VALUES
(1, 1, 'sq', 'Square a number', 'Write a method called "sq" that returns the square of an integer parameter.');

-- --------------------------------------------------------

--
-- Table structure for table `terms`
--

CREATE TABLE IF NOT EXISTS `terms` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL,
  `seq` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=7 ;

--
-- Dumping data for table `terms`
--

INSERT INTO `terms` (`id`, `name`, `seq`) VALUES
(1, 'Winter', 1),
(2, 'Spring', 2),
(3, 'Summer', 3),
(4, 'Summer I', 4),
(5, 'Summer II', 5),
(6, 'Fall', 6);

-- --------------------------------------------------------

--
-- Table structure for table `test_cases`
--

CREATE TABLE IF NOT EXISTS `test_cases` (
  `test_case_id` int(11) NOT NULL AUTO_INCREMENT,
  `problem_id` int(11) NOT NULL,
  `test_case_name` varchar(40) DEFAULT NULL,
  `input` varchar(255) NOT NULL,
  `output` varchar(255) NOT NULL,
  PRIMARY KEY (`test_case_id`),
  KEY `problem_id` (`problem_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=6 ;

--
-- Dumping data for table `test_cases`
--

INSERT INTO `test_cases` (`test_case_id`, `problem_id`, `test_case_name`, `input`, `output`) VALUES
(2, 1, 'test1', '5', '25'),
(3, 1, 'test2', '-1', '1'),
(4, 1, 'test3', '9', '81'),
(5, 1, 'test4', '10', '100');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(20) CHARACTER SET utf8 DEFAULT NULL,
  `password_md5` varchar(32) CHARACTER SET utf8 DEFAULT NULL,
  `salt` varchar(16) CHARACTER SET utf8 DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `users_username_index` (`username`)
) ENGINE=InnoDB  DEFAULT CHARSET=ucs2 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password_md5`, `salt`) VALUES
(1, 'user', '7be1cb12697d993266db952cde9456c6', '5011ffcedffe0a14');
