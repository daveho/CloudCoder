-- phpMyAdmin SQL Dump
-- version 3.4.5deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jan 26, 2012 at 02:27 PM
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
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=982 ;

--
-- Dumping data for table `cc_changes`
--

INSERT INTO `cc_changes` (`id`, `event_id`, `type`, `start_row`, `end_row`, `start_col`, `end_col`, `text`) VALUES
(854, 1004, 4, 0, 0, 0, 0, '#include <stdio.h>\n\nint main(void) {\n	// TODO: add your code\n\n	return 0;\n}'),
(855, 1005, 0, 3, 4, 23, 0, '\n'),
(856, 1006, 0, 4, 4, 0, 4, '    '),
(857, 1007, 0, 4, 4, 4, 5, 'p'),
(858, 1008, 0, 4, 4, 5, 6, 'r'),
(859, 1009, 0, 4, 4, 6, 7, 'i'),
(860, 1010, 0, 4, 4, 7, 8, 'n'),
(861, 1011, 0, 4, 4, 8, 9, 't'),
(862, 1012, 0, 4, 4, 9, 12, 'f("'),
(863, 1013, 0, 4, 4, 12, 13, 'H'),
(864, 1014, 0, 4, 4, 13, 14, 'e'),
(865, 1015, 0, 4, 4, 14, 15, 'l'),
(866, 1016, 0, 4, 4, 15, 16, 'l'),
(867, 1017, 0, 4, 4, 16, 17, 'o'),
(868, 1018, 0, 4, 4, 17, 18, ','),
(869, 1019, 0, 4, 4, 18, 19, ' '),
(870, 1020, 0, 4, 4, 19, 20, 'w'),
(871, 1021, 0, 4, 4, 20, 21, 'o'),
(872, 1022, 0, 4, 4, 21, 22, 'r'),
(873, 1023, 0, 4, 4, 22, 23, 'l'),
(874, 1024, 0, 4, 4, 23, 24, 'd'),
(875, 1025, 0, 4, 4, 24, 25, '"'),
(876, 1026, 1, 4, 4, 24, 25, '"'),
(877, 1027, 0, 4, 4, 24, 25, '\\'),
(878, 1028, 0, 4, 4, 25, 26, 'n'),
(879, 1029, 0, 4, 4, 26, 27, '"'),
(880, 1030, 0, 4, 4, 27, 28, ')'),
(881, 1031, 0, 4, 4, 28, 29, ';'),
(882, 1032, 4, 0, 0, 0, 0, '#include <stdio.h>\n\nint main(void) {\n	// TODO: add your code\n    printf("Hello, world\\n");\n\n	return 0;\n}'),
(883, 1034, 1, 4, 4, 4, 29, 'printf("Hello, world\\n");'),
(884, 1035, 0, 4, 4, 4, 5, 'i'),
(885, 1036, 0, 4, 4, 5, 6, 'n'),
(886, 1037, 0, 4, 4, 6, 7, 't'),
(887, 1038, 0, 4, 4, 7, 8, ' '),
(888, 1039, 0, 4, 4, 8, 9, 'a'),
(889, 1040, 0, 4, 4, 9, 10, ','),
(890, 1041, 0, 4, 4, 10, 11, ';'),
(891, 1042, 1, 4, 4, 10, 11, ';'),
(892, 1043, 0, 4, 4, 10, 11, 'b'),
(893, 1044, 0, 4, 4, 11, 12, ';'),
(894, 1045, 0, 4, 5, 12, 0, '\n'),
(895, 1046, 0, 5, 5, 0, 4, '    '),
(896, 1047, 0, 5, 5, 4, 5, 's'),
(897, 1048, 0, 5, 5, 5, 6, 'c'),
(898, 1049, 0, 5, 5, 6, 7, 'a'),
(899, 1050, 0, 5, 5, 7, 8, 'n'),
(900, 1051, 0, 5, 5, 8, 9, 'f'),
(901, 1052, 0, 5, 5, 9, 11, '()'),
(902, 1053, 0, 5, 5, 10, 12, '""'),
(903, 1054, 0, 5, 5, 11, 12, '%'),
(904, 1055, 0, 5, 5, 12, 13, 'i'),
(905, 1056, 0, 5, 5, 13, 14, '\\'),
(906, 1057, 1, 5, 5, 13, 14, '\\'),
(907, 1058, 0, 5, 5, 13, 14, ','),
(908, 1059, 1, 5, 5, 13, 14, ','),
(909, 1060, 0, 5, 5, 14, 15, ','),
(910, 1061, 0, 5, 5, 15, 16, ' '),
(911, 1062, 0, 5, 5, 16, 17, '&'),
(912, 1063, 0, 5, 5, 17, 18, 'a'),
(913, 1064, 0, 5, 5, 19, 20, ';'),
(914, 1065, 0, 5, 6, 20, 0, '\n'),
(915, 1066, 0, 6, 6, 0, 4, '    '),
(916, 1067, 0, 6, 6, 4, 5, 's'),
(917, 1068, 0, 6, 6, 5, 6, 'c'),
(918, 1069, 0, 6, 6, 6, 7, 'a'),
(919, 1070, 0, 6, 6, 7, 8, 'n'),
(920, 1071, 0, 6, 6, 8, 9, 'f'),
(921, 1072, 0, 6, 6, 9, 11, '()'),
(922, 1073, 0, 6, 6, 10, 12, '""'),
(923, 1074, 0, 6, 6, 11, 12, '%'),
(924, 1075, 0, 6, 6, 12, 13, 'i'),
(925, 1076, 0, 6, 6, 14, 15, ','),
(926, 1077, 0, 6, 6, 15, 16, ' '),
(927, 1078, 0, 6, 6, 16, 17, '&'),
(928, 1079, 0, 6, 6, 17, 18, 'b'),
(929, 1080, 0, 6, 6, 19, 20, ';'),
(930, 1081, 0, 6, 7, 20, 0, '\n'),
(931, 1082, 0, 7, 7, 0, 4, '    '),
(932, 1083, 0, 7, 7, 4, 5, 'p'),
(933, 1084, 0, 7, 7, 5, 6, 'r'),
(934, 1085, 0, 7, 7, 6, 7, 'i'),
(935, 1086, 0, 7, 7, 7, 8, 'n'),
(936, 1087, 0, 7, 7, 8, 9, 't'),
(937, 1088, 0, 7, 7, 9, 10, 'f'),
(938, 1089, 0, 7, 7, 10, 12, '()'),
(939, 1090, 0, 7, 7, 11, 13, '""'),
(940, 1091, 0, 7, 7, 12, 13, 'a'),
(941, 1092, 0, 7, 7, 13, 14, 'n'),
(942, 1093, 0, 7, 7, 14, 15, 's'),
(943, 1094, 0, 7, 7, 15, 16, 'w'),
(944, 1095, 0, 7, 7, 16, 17, 'e'),
(945, 1096, 0, 7, 7, 17, 18, 'r'),
(946, 1097, 0, 7, 7, 18, 19, ':'),
(947, 1098, 0, 7, 7, 19, 20, ' '),
(948, 1099, 0, 7, 7, 20, 21, '%'),
(949, 1100, 0, 7, 7, 21, 22, 'i'),
(950, 1101, 0, 7, 7, 22, 23, '\\'),
(951, 1102, 0, 7, 7, 23, 24, 'n'),
(952, 1103, 0, 7, 7, 25, 26, ','),
(953, 1104, 0, 7, 7, 26, 27, ' '),
(954, 1105, 0, 7, 7, 27, 29, '()'),
(955, 1106, 0, 7, 7, 28, 29, 'a'),
(956, 1107, 0, 7, 7, 29, 30, '+'),
(957, 1108, 0, 7, 7, 30, 31, 'b'),
(958, 1109, 0, 7, 7, 33, 34, ';'),
(959, 1110, 4, 0, 0, 0, 0, '#include <stdio.h>\n\nint main(void) {\n	// TODO: add your code\n    int a,b;\n    scanf("%i", &a);\n    scanf("%i", &b);\n    printf("answer: %i\\n", (a+b));\n\n	return 0;\n}'),
(960, 1112, 0, 3, 4, 23, 0, '\n'),
(961, 1113, 0, 4, 4, 0, 4, '    '),
(962, 1114, 0, 4, 4, 4, 5, 'p'),
(963, 1115, 0, 4, 4, 5, 6, 'r'),
(964, 1116, 0, 4, 4, 6, 7, 'i'),
(965, 1117, 0, 4, 4, 7, 8, 'n'),
(966, 1118, 0, 4, 4, 8, 9, 't'),
(967, 1119, 0, 4, 4, 9, 10, 'f'),
(968, 1120, 0, 4, 4, 10, 12, '()'),
(969, 1121, 0, 4, 4, 11, 13, '""'),
(970, 1122, 0, 4, 4, 12, 13, 'm'),
(971, 1123, 0, 4, 4, 13, 14, 'e'),
(972, 1124, 0, 4, 4, 14, 15, 'e'),
(973, 1125, 0, 4, 4, 15, 16, 'p'),
(974, 1126, 0, 4, 4, 16, 17, 's'),
(975, 1127, 0, 4, 4, 17, 18, 'i'),
(976, 1128, 0, 4, 4, 18, 19, 'e'),
(977, 1129, 0, 4, 4, 19, 20, '!'),
(978, 1130, 0, 4, 4, 20, 21, '\\'),
(979, 1131, 0, 4, 4, 21, 22, 'n'),
(980, 1132, 0, 4, 4, 24, 25, ';'),
(981, 1133, 4, 0, 0, 0, 0, '#include <stdio.h>\n\nint main(void) {\n	// TODO: add your code\n    printf("meepsie!\\n");\n    int a,b;\n    scanf("%i", &a);\n    scanf("%i", &b);\n    printf("answer: %i\\n", (a+b));\n\n	return 0;\n}\n');

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
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=12 ;

--
-- Dumping data for table `cc_course_registrations`
--

INSERT INTO `cc_course_registrations` (`id`, `course_id`, `user_id`, `registration_type`) VALUES
(5, 1, 2, 0),
(6, 1, 3, 0),
(7, 1, 4, 0),
(8, 1, 5, 0),
(9, 1, 6, 0),
(10, 1, 7, 0),
(11, 1, 8, 0);

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
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=1135 ;

--
-- Dumping data for table `cc_events`
--

INSERT INTO `cc_events` (`id`, `user_id`, `problem_id`, `type`, `timestamp`) VALUES
(1003, 4, 2, 2, 1327426057272),
(1004, 4, 2, 0, 1327426058766),
(1005, 4, 2, 0, 1327426064843),
(1006, 4, 2, 0, 1327426064855),
(1007, 4, 2, 0, 1327426065186),
(1008, 4, 2, 0, 1327426065270),
(1009, 4, 2, 0, 1327426065365),
(1010, 4, 2, 0, 1327426065410),
(1011, 4, 2, 0, 1327426065469),
(1012, 4, 2, 0, 1327426067107),
(1013, 4, 2, 0, 1327426067137),
(1014, 4, 2, 0, 1327426067304),
(1015, 4, 2, 0, 1327426067386),
(1016, 4, 2, 0, 1327426067514),
(1017, 4, 2, 0, 1327426067672),
(1018, 4, 2, 0, 1327426067863),
(1019, 4, 2, 0, 1327426067941),
(1020, 4, 2, 0, 1327426068001),
(1021, 4, 2, 0, 1327426068097),
(1022, 4, 2, 0, 1327426068232),
(1023, 4, 2, 0, 1327426068416),
(1024, 4, 2, 0, 1327426068478),
(1025, 4, 2, 0, 1327426068823),
(1026, 4, 2, 0, 1327426069872),
(1027, 4, 2, 0, 1327426070368),
(1028, 4, 2, 0, 1327426070543),
(1029, 4, 2, 0, 1327426070837),
(1030, 4, 2, 0, 1327426071055),
(1031, 4, 2, 0, 1327426071212),
(1032, 4, 2, 0, 1327426098649),
(1033, 4, 2, 2, 1327426101807),
(1034, 4, 2, 0, 1327426138348),
(1035, 4, 2, 0, 1327426138358),
(1036, 4, 2, 0, 1327426138400),
(1037, 4, 2, 0, 1327426138566),
(1038, 4, 2, 0, 1327426138620),
(1039, 4, 2, 0, 1327426138658),
(1040, 4, 2, 0, 1327426138823),
(1041, 4, 2, 0, 1327426139126),
(1042, 4, 2, 0, 1327426139647),
(1043, 4, 2, 0, 1327426140079),
(1044, 4, 2, 0, 1327426140142),
(1045, 4, 2, 0, 1327426140284),
(1046, 4, 2, 0, 1327426140291),
(1047, 4, 2, 0, 1327426141097),
(1048, 4, 2, 0, 1327426141152),
(1049, 4, 2, 0, 1327426141297),
(1050, 4, 2, 0, 1327426141370),
(1051, 4, 2, 0, 1327426141465),
(1052, 4, 2, 0, 1327426141646),
(1053, 4, 2, 0, 1327426141776),
(1054, 4, 2, 0, 1327426142817),
(1055, 4, 2, 0, 1327426142976),
(1056, 4, 2, 0, 1327426143180),
(1057, 4, 2, 0, 1327426143886),
(1058, 4, 2, 0, 1327426144115),
(1059, 4, 2, 0, 1327426144498),
(1060, 4, 2, 0, 1327426145086),
(1061, 4, 2, 0, 1327426145149),
(1062, 4, 2, 0, 1327426146087),
(1063, 4, 2, 0, 1327426146209),
(1064, 4, 2, 0, 1327426146596),
(1065, 4, 2, 0, 1327426146806),
(1066, 4, 2, 0, 1327426146812),
(1067, 4, 2, 0, 1327426147361),
(1068, 4, 2, 0, 1327426147441),
(1069, 4, 2, 0, 1327426147570),
(1070, 4, 2, 0, 1327426147637),
(1071, 4, 2, 0, 1327426147697),
(1072, 4, 2, 0, 1327426147962),
(1073, 4, 2, 0, 1327426148082),
(1074, 4, 2, 0, 1327426148915),
(1075, 4, 2, 0, 1327426149092),
(1076, 4, 2, 0, 1327426150145),
(1077, 4, 2, 0, 1327426150664),
(1078, 4, 2, 0, 1327426150800),
(1079, 4, 2, 0, 1327426151315),
(1080, 4, 2, 0, 1327426151844),
(1081, 4, 2, 0, 1327426151996),
(1082, 4, 2, 0, 1327426152003),
(1083, 4, 2, 0, 1327426152775),
(1084, 4, 2, 0, 1327426152848),
(1085, 4, 2, 0, 1327426152948),
(1086, 4, 2, 0, 1327426152989),
(1087, 4, 2, 0, 1327426153064),
(1088, 4, 2, 0, 1327426153196),
(1089, 4, 2, 0, 1327426153376),
(1090, 4, 2, 0, 1327426153499),
(1091, 4, 2, 0, 1327426153909),
(1092, 4, 2, 0, 1327426154053),
(1093, 4, 2, 0, 1327426154152),
(1094, 4, 2, 0, 1327426154708),
(1095, 4, 2, 0, 1327426154895),
(1096, 4, 2, 0, 1327426154957),
(1097, 4, 2, 0, 1327426155277),
(1098, 4, 2, 0, 1327426155566),
(1099, 4, 2, 0, 1327426156428),
(1100, 4, 2, 0, 1327426156576),
(1101, 4, 2, 0, 1327426156765),
(1102, 4, 2, 0, 1327426156958),
(1103, 4, 2, 0, 1327426157698),
(1104, 4, 2, 0, 1327426158305),
(1105, 4, 2, 0, 1327426159145),
(1106, 4, 2, 0, 1327426159282),
(1107, 4, 2, 0, 1327426159639),
(1108, 4, 2, 0, 1327426159859),
(1109, 4, 2, 0, 1327426160508),
(1110, 4, 2, 0, 1327426162705),
(1111, 4, 2, 2, 1327426165612),
(1112, 4, 2, 0, 1327605939361),
(1113, 4, 2, 0, 1327605939370),
(1114, 4, 2, 0, 1327605939800),
(1115, 4, 2, 0, 1327605939882),
(1116, 4, 2, 0, 1327605940000),
(1117, 4, 2, 0, 1327605940028),
(1118, 4, 2, 0, 1327605940085),
(1119, 4, 2, 0, 1327605940258),
(1120, 4, 2, 0, 1327605940428),
(1121, 4, 2, 0, 1327605940609),
(1122, 4, 2, 0, 1327605941501),
(1123, 4, 2, 0, 1327605941690),
(1124, 4, 2, 0, 1327605941838),
(1125, 4, 2, 0, 1327605941900),
(1126, 4, 2, 0, 1327605942140),
(1127, 4, 2, 0, 1327605942239),
(1128, 4, 2, 0, 1327605942323),
(1129, 4, 2, 0, 1327605942654),
(1130, 4, 2, 0, 1327605943030),
(1131, 4, 2, 0, 1327605943656),
(1132, 4, 2, 0, 1327605944302),
(1133, 4, 2, 0, 1327606021752),
(1134, 4, 2, 2, 1327606026839);

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
  `description` varchar(255) NOT NULL,
  `when_assigned` bigint(20) NOT NULL,
  `when_due` bigint(20) NOT NULL,
  `skeleton` varchar(400) DEFAULT NULL,
  PRIMARY KEY (`problem_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=3 ;

--
-- Dumping data for table `cc_problems`
--

INSERT INTO `cc_problems` (`problem_id`, `course_id`, `problem_type`, `testname`, `brief_description`, `description`, `when_assigned`, `when_due`, `skeleton`) VALUES
(1, 1, 0, 'sq', 'Square a number', 'Write a method called "sq" that returns the square of an integer parameter.', 0, 0, NULL),
(2, 1, 3, 'addInts', 'read/add/print sum of integers', 'Read two integer values, and then print "answer: X" where X is the sum of the two integer values.', 1326485534072, 1326571934072, '#include <stdio.h>\n\nint main(void) {\n	// TODO: add your code\n\n	return 0;\n}');

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
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=115 ;

--
-- Dumping data for table `cc_submission_receipts`
--

INSERT INTO `cc_submission_receipts` (`id`, `event_id`, `last_edit_event_id`, `status`) VALUES
(111, 1003, -1, 4),
(112, 1033, 1032, 1),
(113, 1111, 1110, 0),
(114, 1134, 1133, 0);

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
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=11 ;

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
(10, 2, 'negOnePlusNeg2', '-1 -2', '^\\s*answer\\s*:\\s*-(0*)3\\s*$', 1);

-- --------------------------------------------------------

--
-- Table structure for table `cc_test_results`
--

CREATE TABLE IF NOT EXISTS `cc_test_results` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `submission_receipt_id` int(11) NOT NULL,
  `test_outcome` int(11) NOT NULL,
  `message` varchar(100) NOT NULL,
  `stdout` varchar(200) NOT NULL,
  `stderr` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `submission_receipt_id` (`submission_receipt_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=253 ;

--
-- Dumping data for table `cc_test_results`
--

INSERT INTO `cc_test_results` (`id`, `submission_receipt_id`, `test_outcome`, `message`, `stdout`, `stderr`) VALUES
(238, 0, 1, 'Test failed for input (4 5)', 'Hello, world\n', ''),
(239, 0, 1, 'Test failed for input (6 7)', 'Hello, world\n', ''),
(240, 0, 1, 'Test failed for input (3 -17)', 'Hello, world\n', ''),
(241, 0, 1, 'Test failed', 'Hello, world\n', ''),
(242, 0, 1, 'Test failed', 'Hello, world\n', ''),
(243, 0, 0, 'Test passed for input (4 5)', 'answer: 9\n', ''),
(244, 0, 0, 'Test passed for input (6 7)', 'answer: 13\n', ''),
(245, 0, 0, 'Test passed for input (3 -17)', 'answer: -14\n', ''),
(246, 0, 0, 'Test passed', 'answer: 1\n', ''),
(247, 0, 0, 'Test passed', 'answer: -3\n', ''),
(248, 0, 0, 'Test passed for input (4 5)', 'meepsie!\nanswer: 9\n', ''),
(249, 0, 0, 'Test passed for input (6 7)', 'meepsie!\nanswer: 13\n', ''),
(250, 0, 0, 'Test passed for input (3 -17)', 'meepsie!\nanswer: -14\n', ''),
(251, 0, 0, 'Test passed', 'meepsie!\nanswer: 1\n', ''),
(252, 0, 0, 'Test passed', 'meepsie!\nanswer: -3\n', '');

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
