-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: Bridge
-- ------------------------------------------------------
-- Server version	8.0.46-0ubuntu0.24.04.4

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `Bridge`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `Bridge` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `Bridge`;

--
-- Table structure for table `bids`
--

DROP TABLE IF EXISTS `bids`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bids` (
  `bid_id` int NOT NULL AUTO_INCREMENT,
  `game_id` int NOT NULL,
  `bid_value` varchar(10) NOT NULL,
  `position` varchar(10) NOT NULL,
  PRIMARY KEY (`bid_id`),
  KEY `game_id` (`game_id`),
  KEY `player_id` (`position`),
  CONSTRAINT `bids_ibfk_1` FOREIGN KEY (`game_id`) REFERENCES `games` (`game_id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bids`
--

LOCK TABLES `bids` WRITE;
/*!40000 ALTER TABLE `bids` DISABLE KEYS */;
INSERT INTO `bids` VALUES (1,22,'1♣','South'),(2,22,'2♣','West'),(3,22,'Pass','North'),(4,22,'Pass','East'),(5,22,'Pass','South'),(6,23,'1♣','South'),(7,23,'2♣','West'),(8,23,'Pass','North'),(9,23,'Pass','East'),(10,23,'Pass','South'),(11,24,'1♣','South'),(12,24,'Pass','West'),(13,24,'Pass','North'),(14,24,'Pass','East'),(15,25,'1♣','South'),(16,25,'2♣','West'),(17,25,'Double','North'),(18,25,'Pass','East'),(19,25,'Pass','South'),(20,25,'Pass','West'),(21,26,'1♣','South'),(22,26,'2♣','West'),(23,26,'Pass','North'),(24,26,'Pass','East'),(25,26,'Pass','South'),(26,28,'1♣','South'),(27,28,'Pass','West'),(28,28,'Pass','North'),(29,28,'Pass','East');
/*!40000 ALTER TABLE `bids` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cards_played`
--

DROP TABLE IF EXISTS `cards_played`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cards_played` (
  `cards_id` int NOT NULL AUTO_INCREMENT,
  `suit` varchar(10) NOT NULL,
  `card_rank` varchar(5) NOT NULL,
  `trick_id` int NOT NULL,
  `play_order` int NOT NULL,
  PRIMARY KEY (`cards_id`),
  KEY `trick_id` (`trick_id`),
  CONSTRAINT `cards_played_ibfk_2` FOREIGN KEY (`trick_id`) REFERENCES `tricks` (`trick_id`)
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cards_played`
--

LOCK TABLES `cards_played` WRITE;
/*!40000 ALTER TABLE `cards_played` DISABLE KEYS */;
INSERT INTO `cards_played` VALUES (1,'C','8',1,1),(2,'C','A',1,2),(3,'C','7',1,3),(4,'C','9',1,4),(5,'D','5',2,1),(6,'D','4',2,2),(7,'D','A',2,3),(8,'D','10',2,4),(9,'D','J',3,1),(10,'S','6',3,2),(11,'D','6',3,3),(12,'D','8',3,4),(13,'D','8',4,1),(14,'D','7',4,2),(15,'D','9',4,3),(16,'D','6',4,4),(17,'S','A',5,1),(18,'S','J',5,2),(19,'S','7',5,3),(20,'S','K',5,4),(21,'S','3',6,1),(22,'D','2',6,2),(23,'S','4',6,3),(24,'S','8',6,4),(25,'S','2',7,1),(26,'S','5',7,2),(27,'D','4',7,3),(28,'S','Q',7,4),(29,'D','6',8,1),(30,'D','J',8,2),(31,'D','K',8,3),(32,'D','9',8,4),(33,'D','10',9,1),(34,'D','7',9,2),(35,'D','3',9,3),(36,'D','A',9,4);
/*!40000 ALTER TABLE `cards_played` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `games`
--

DROP TABLE IF EXISTS `games`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `games` (
  `game_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `dealer` int DEFAULT NULL,
  `declarer` int DEFAULT NULL,
  `seq_num` int DEFAULT NULL,
  `attempt_num` int DEFAULT NULL,
  `date_played` date DEFAULT NULL,
  PRIMARY KEY (`game_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `games_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `games`
--

LOCK TABLES `games` WRITE;
/*!40000 ALTER TABLE `games` DISABLE KEYS */;
INSERT INTO `games` VALUES (1,1,1,NULL,1,1,'2026-09-13'),(2,1,1,NULL,2,1,'2026-09-13'),(3,1,2,NULL,3,1,'2026-09-13'),(4,1,3,NULL,4,1,'2026-09-13'),(5,1,2,NULL,5,1,'2026-09-13'),(6,1,0,NULL,6,1,'2026-09-13'),(7,1,0,NULL,7,1,'2026-09-13'),(8,1,0,NULL,8,1,'2026-09-13'),(9,1,0,NULL,1,1,'2026-09-15'),(10,1,0,NULL,2,1,'2026-09-15'),(11,1,2,NULL,3,1,'2026-09-15'),(12,1,0,NULL,4,1,'2026-09-15'),(13,1,1,NULL,5,1,'2026-09-15'),(14,1,0,NULL,1,1,'2026-09-16'),(15,1,0,NULL,2,1,'2026-09-16'),(16,1,0,NULL,3,1,'2026-09-16'),(17,1,0,NULL,4,1,'2026-09-16'),(18,1,0,NULL,5,1,'2026-09-16'),(19,1,2,NULL,6,1,'2026-09-16'),(20,1,0,NULL,7,1,'2026-09-16'),(21,1,0,0,8,1,'2026-09-16'),(22,1,0,1,9,1,'2026-09-16'),(23,1,0,1,10,1,'2026-09-16'),(24,1,0,0,11,1,'2026-09-16'),(25,1,0,1,12,1,'2026-09-16'),(26,1,0,1,13,1,'2026-09-16'),(27,1,0,NULL,1,1,'2026-09-17'),(28,1,0,0,2,1,'2026-09-17');
/*!40000 ALTER TABLE `games` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tricks`
--

DROP TABLE IF EXISTS `tricks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tricks` (
  `trick_id` int NOT NULL AUTO_INCREMENT,
  `game_id` int NOT NULL,
  `trick_number` int NOT NULL,
  `winner` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`trick_id`),
  KEY `game_id` (`game_id`),
  CONSTRAINT `tricks_ibfk_1` FOREIGN KEY (`game_id`) REFERENCES `games` (`game_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tricks`
--

LOCK TABLES `tricks` WRITE;
/*!40000 ALTER TABLE `tricks` DISABLE KEYS */;
INSERT INTO `tricks` VALUES (1,24,1,'NONE'),(2,25,1,'NORTH_SOUTH'),(3,25,2,'NORTH_SOUTH'),(4,26,1,'NORTH_SOUTH'),(5,26,2,'NORTH_SOUTH'),(6,26,3,'EAST_WEST'),(7,26,4,'NORTH_SOUTH'),(8,28,1,'EAST_WEST'),(9,28,2,'NORTH_SOUTH');
/*!40000 ALTER TABLE `tricks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'Joye12','$2b$12$C3QkxFBWu/BhPiNKBfKWSecFj2yizHwvQ/Bdo1rC8M7KOEPbtpAXS'),(2,'Josh','$2b$12$FT/F7wVKo8MeCqoqwvyPkekuX8taEtZuEQbZBf/aMFqt3iKcFgBhS'),(3,'test_user_f9e1667c','$2b$12$QsKnAdtW.9XZ7WzbfaG2huyPN/0OsNLdC4X.Pqhu05qWMG33K7fzK'),(4,'new_test_user_84f60e69','$2b$12$94rqY9lFq8rF7Bk18ouyqO/Vy5ZqPkFSV5QfrZuSqVq0O4qhN2TCW'),(5,'test_user_c612efb2','$2b$12$o5.RUXE2Al7FBfmx/yDEt.B.0nvkrNwZ5Nf45Jcv.4Hz42cb/GBZK'),(6,'new_test_user_5e0d2125','$2b$12$rG9E.upusj6lYnUoJgjPQuTCrulyBAPJobCs9e47jbRR71ENrlXKS'),(7,'test_user_70c4bcaa','$2b$12$Nft8pea6YHIMRPkjFeAceu9fxLxQtjcTlhv8jARqq6L7jOhRFjjja'),(8,'new_test_user_1e417449','$2b$12$JsUlzShtnv8Bidcc3CdoVuCdAkb7xJ4DNLTUt5.csvkqyFuxgNpuW'),(9,'test_user_45221517','$2b$12$sYHv4UAlpf8wNHJGEVkahe1hN5LjXWDYWaOUaYBV4yPyMUuRg0AnO'),(10,'new_test_user_d9d00713','$2b$12$hpCX/GFnd4nG/cDRibmujeRTSbJCqUFuuDiDpfyUUtBddSCxJspde');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-17 14:46:33
