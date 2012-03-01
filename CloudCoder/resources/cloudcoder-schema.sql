-- phpMyAdmin SQL Dump
-- version 3.4.5deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Feb 19, 2012 at 12:41 PM
-- Server version: 5.1.58
-- PHP Version: 5.3.6-13ubuntu3.6

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
  `text` text NOT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;

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
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=124 ;

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
(48, 2, 'test7', '-14 29', '^\\s*answer\\s*:\\s*(0+)?15\\s*$', 1),
(49, 12, 'm5n3', '5 3', '^\\s*AAAAABBBBBAAAAABBBBBAAAAABBBBB\\s*$', 0),
(50, 12, 'm3n4', '3 4', '^\\s*AAABBBAAABBBAAABBB\\s*$', 0),
(51, 12, 'm4n5', '4 5', '^\\s*AAAABBBBAAAABBBBAAAABBBBAAAABBBBAAAABBBB\\s*$', 0),
(52, 12, 'm1n8', '1 8', '^\\s*ABABABABABABABAB\\s*$', 1),
(53, 12, 'm0n8', '0 8', '^\\s*$', 1),
(54, 13, 'm5n3', '5 3', '^\\s*AAAAABBBBBAAAAABBBBBAAAAABBBBB\\s*$', 0),
(55, 13, 'm3n4', '3 4', '^\\s*AAABBBAAABBBAAABBBAAABBB\\s*$', 0),
(56, 13, 'm4n5', '4 5', '^\\s*AAAABBBBAAAABBBBAAAABBBBAAAABBBBAAAABBBB\\s*$', 0),
(57, 13, 'm1n8', '1 8', '^\\s*ABABABABABABABAB\\s*$', 1),
(58, 13, 'm0n8', '0 8', '^\\s*$', 1),
(59, 14, 'n19', '19', '^\\s*[Aa]nswer:\\s*1\\s*3\\s*5\\s*7\\s*9\\s*11\\s*13\\s*15\\s*17\\s*19\\s*$', 0),
(60, 14, 'n11', '11', '^\\s*[Aa]nswer:\\s*1\\s*3\\s*5\\s*7\\s*9\\s*11$', 0),
(61, 14, 'n20', '20', '^\\s*[Aa]nswer:\\s*1\\s*3\\s*5\\s*7\\s*9\\s*11\\s*13\\s*15\\s*17\\s*19\\s*$', 0),
(62, 14, 'n1', '1', '^\\s*[Aa]nswer:\\s*1\\s*$', 0),
(63, 14, 'n0', '1', '^\\s*[Aa]nswer:\\s*$', 0),
(64, 15, 'n19', '19', '^\\s*[Aa]nswer:\\s*1\\s*3\\s*5\\s*7\\s*9\\s*11\\s*13\\s*15\\s*17\\s*19\\s*$', 0),
(65, 15, 'n11', '11', '^\\s*[Aa]nswer:\\s*1\\s*3\\s*5\\s*7\\s*9\\s*11$', 0),
(66, 15, 'n20', '20', '^\\s*[Aa]nswer:\\s*1\\s*3\\s*5\\s*7\\s*9\\s*11\\s*13\\s*15\\s*17\\s*19\\s*$', 0),
(67, 15, 'n1', '1', '^\\s*[Aa]nswer:\\s*1\\s*$', 0),
(68, 15, 'n0', '0', '^\\s*[Aa]nswer:\\s*$', 0),
(69, 16, 'n19', '19', '^\\s*[Aa]nswer:\\s*1\\s*3\\s*5\\s*7\\s*9\\s*11\\s*13\\s*15\\s*17\\s*19\\s*$', 0),
(70, 16, 'n11', '11', '^\\s*[Aa]nswer:\\s*1\\s*3\\s*5\\s*7\\s*9\\s*11\\s*$', 0),
(71, 16, 'n20', '20', '^\\s*[Aa]nswer:\\s*1\\s*3\\s*5\\s*7\\s*9\\s*11\\s*13\\s*15\\s*17\\s*19\\s*$', 0),
(72, 16, 'n1', '1', '^\\s*[Aa]nswer:\\s*1\\s*$', 0),
(73, 16, 'n0', '0', '^\\s*[Aa]nswer:\\s*$', 0),
(74, 17, 'n19', '19', '^\\s*[Aa]nswer:\\s*1\\s*3\\s*5\\s*7\\s*9\\s*11\\s*13\\s*15\\s*17\\s*19\\s*$', 0),
(75, 17, 'n11', '11', '^\\s*[Aa]nswer:\\s*1\\s*3\\s*5\\s*7\\s*9\\s*11\\s*$', 0),
(76, 17, 'n20', '20', '^\\s*[Aa]nswer:\\s*1\\s*3\\s*5\\s*7\\s*9\\s*11\\s*13\\s*15\\s*17\\s*19\\s*$', 0),
(77, 17, 'n1', '1', '^\\s*[Aa]nswer:\\s*1\\s*$', 0),
(78, 17, 'n0', '0', '^\\s*[Aa]nswer:\\s*$', 0),
(79, 18, 'm5n3', '5 3', '^\\s*AAAAABBBBBAAAAABBBBBAAAAABBBBB\\s*$', 0),
(80, 18, 'm3n4', '3 4', '^\\s*AAABBBAAABBBAAABBBAAABBB\\s*$', 0),
(81, 18, 'm4n5', '4 5', '^\\s*AAAABBBBAAAABBBBAAAABBBBAAAABBBBAAAABBBB\\s*$', 0),
(82, 18, 'm1n8', '1 8', '^\\s*ABABABABABABABAB\\s*$', 1),
(83, 18, 'm0n8', '0 8', '^\\s*$', 1),
(84, 19, 'n20', '20', '^\\s*[Aa]nswer:2\\s*4\\s*8\\s*10\\s*14\\s*16\\s*20\\s*$', 0),
(85, 19, 'n12', '12', '^\\s*[Aa]nswer:\\s*2\\s*4\\s*8\\s*10\\s*$', 0),
(86, 19, 'n30', '30', '^\\s*[Aa]nswer:\\s*2\\s*4\\s*8\\s*10\\s*14\\s*16\\s*20\\s*22\\s*26\\s*28\\s*$', 0),
(87, 19, 'n2', '2', '^\\s*[Aa]nswer:\\s*2\\s*$', 0),
(88, 19, 'n0', '0', '^\\s*[Aa]nswer:\\s*$', 0),
(89, 20, 'n20', '20', '^\\s*[Aa]nswer:2\\s*4\\s*8\\s*10\\s*14\\s*16\\s*20\\s*$', 0),
(90, 20, 'n12', '12', '^\\s*[Aa]nswer:\\s*2\\s*4\\s*8\\s*10\\s*$', 0),
(91, 20, 'n30', '30', '^\\s*[Aa]nswer:\\s*2\\s*4\\s*8\\s*10\\s*14\\s*16\\s*20\\s*22\\s*26\\s*28\\s*$', 0),
(92, 20, 'n2', '2', '^\\s*[Aa]nswer:\\s*2\\s*$', 0),
(93, 20, 'n0', '0', '^\\s*[Aa]nswer:\\s*$', 0),
(94, 21, 'n20', '20', '^\\s*[Aa]nswer:\\s*2\\s*4\\s*8\\s*10\\s*14\\s*16\\s*20\\s*$', 0),
(95, 21, 'n12', '12', '^\\s*[Aa]nswer:\\s*2\\s*4\\s*8\\s*10\\s*$', 0),
(96, 21, 'n30', '30', '^\\s*[Aa]nswer:\\s*2\\s*4\\s*8\\s*10\\s*14\\s*16\\s*20\\s*22\\s*26\\s*28\\s*$', 0),
(97, 21, 'n2', '2', '^\\s*[Aa]nswer:\\s*2\\s*$', 0),
(98, 21, 'n0', '0', '^\\s*[Aa]nswer:\\s*$', 0),
(99, 22, 'value3_71', '3.71', '[Aa]nswer:\\s*(0*)3\\.71000\\s*$', 0),
(100, 22, 'value753_331', '753.331', '[Aa]nswer:\\s*(0*)1506\\.662000\\s*$', 0),
(101, 22, 'valueneg_901_17', '-901.17', '[Aa]nswer:\\s*-(0*)1802.340000\\s*$', 0),
(102, 23, 'value3_71', '3.71', '[Aa]nswer:\\s*(0*)7\\.420000\\s*$', 0),
(103, 23, 'value753_331', '753.331', '[Aa]nswer:\\s*(0*)1506\\.662000\\s*$', 0),
(104, 23, 'valueneg_901_17', '-901.17', '[Aa]nswer:\\s*-(0*)1802.340000\\s*$', 0),
(105, 24, 'value3_71', '3.71', '^.*[Aa]nswer:\\s*(0*)7\\.420000\\s*$', 0),
(106, 24, 'value753_331', '753.331', '^.*[Aa]nswer:\\s*(0*)1506\\.662000\\s*$', 0),
(107, 24, 'valueneg_901_17', '-901.17', '^.*[Aa]nswer:\\s*-(0*)1802.340000\\s*$', 0),
(108, 25, 'age15', '15', '^.*[Yy]ou\\s+cannot\\s+vote\\s*,\\s*sorry\\s*(\\.)?\\s*$', 0),
(109, 25, 'age26', '26', '^.*[Yy]ou\\s+can\\s+vote\\s*,\\s*congratulations\\s*(\\.)?\\s*$', 0),
(110, 25, 'age4', '4', '^.*[Yy]ou\\s+cannot\\s+vote\\s*,\\s*sorry\\s*(\\.)?\\s*$', 0),
(111, 25, 'age99', '99', '^.*[Yy]ou\\s+can\\s+vote\\s*,\\s*congratulations\\s*(\\.)?\\s*$', 0),
(112, 26, 'age15', '15', '^.*[Yy]ou\\s+cannot\\s+vote\\s*,\\s*sorry\\s*(\\.)?\\s*$', 0),
(113, 26, 'age26', '26', '^.*[Yy]ou\\s+can\\s+vote\\s*,\\s*congratulations\\s*(\\.)?\\s*$', 0),
(114, 26, 'age4', '4', '^.*[Yy]ou\\s+cannot\\s+vote\\s*,\\s*sorry\\s*(\\.)?\\s*$', 0),
(115, 26, 'age99', '99', '^.*[Yy]ou\\s+can\\s+vote\\s*,\\s*congratulations\\s*(\\.)?\\s*$', 0),
(116, 27, 'nEquals7', '7', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*6\\s*7\\s*$', 0),
(117, 27, 'nEquals4', '4', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*$', 0),
(118, 27, 'nEquals5', '5', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*$', 0),
(119, 27, 'nEquals9', '9', '^\\s*\\s*1\\s*2\\s*3\\s*4\\s*5\\s*6\\s*7\\s*8\\s*9\\s*$', 1),
(120, 28, 'n15', '15', '^.*\\s*3\\s+6\\s+9\\s+12\\s+15\\s*$', 0),
(121, 28, 'n23', '23', '^.*\\s*3\\s+6\\s+9\\s+12\\s+15\\s+18\\s+21\\s*$', 0),
(122, 28, 'n7', '7', '^.*\\s*3\\s+6\\s*$', 0),
(123, 28, 'n29', '29', '^.*\\s*3\\s+6\\s+9\\s+12\\s+15\\s+18\\s+21\\s+24\\s+27\\s*$', 1);

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
(4, 'dhovemey', 'a790a0c6ff0f9f89c482e827eb6a2873', '64c51d57faf2954c'),
(5, 'mmmiller', 'a790a0c6ff0f9f89c482e827eb6a2873', '64c51d57faf2954c'),
(6, 'jspacco', 'a790a0c6ff0f9f89c482e827eb6a2873', '64c51d57faf2954c');

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
