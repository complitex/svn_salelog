/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- Call girl --
DROP TABLE IF EXISTS `call_girl`;
CREATE TABLE `call_girl` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(255) NOT NULL UNIQUE,
  `first_name` VARCHAR(255),
  `last_name` VARCHAR(255),
  `middle_name` VARCHAR(255),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Product --
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(255) NOT NULL UNIQUE KEY,
  `name` VARCHAR(200),
  `price` decimal(19,2),
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Order status --
DROP TABLE IF EXISTS `order_status`;
CREATE TABLE `order_status` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `code` BIGINT(20) NOT NULL UNIQUE KEY,
  `name` VARCHAR(255),
  PRIMARY KEY (`pk_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Order --
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `object_id` BIGINT(20) NOT NULL,
  `create_date` TIMESTAMP NOT NULL DEFAULT 0,
  `call_girl_id` BIGINT(20) NOT NULL,
  `first_name` VARCHAR(255) NOT NULL,
  `last_name` VARCHAR(255) NOT NULL ,
  `middle_name` VARCHAR(255),
  `phones` VARCHAR(2000) NOT NULL,
  `regionId` BIGINT(20) NOT NULL,
  `address` VARCHAR(2000) NOT NULL,
  `comment` VARCHAR(2000) NOT NULL,
  `order_status_code` BIGINT(20) NOT NULL DEFAULT 0,
  `begin_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_object_id__begin_date` (`object_id`,`begin_date`),
  KEY `key_object_id` (`object_id`),
  KEY `key_call_girl_id` (`call_girl_id`),
  KEY `key_begin_date` (`begin_date`),
  KEY `key_end_date` (`end_date`),
  CONSTRAINT `fk_order__call_girl` FOREIGN KEY (`call_girl_id`) REFERENCES `call_girl` (`id`),
  CONSTRAINT `fk_order__order_status_code` FOREIGN KEY (`order_status_code`) REFERENCES `order_status` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Collection Product Sale --
DROP TABLE IF EXISTS `product_sale`;
CREATE TABLE `product_sale` (
  `pk_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT(20) NOT NULL,
  `product_id` BIGINT(20) NOT NULL,
  `price` decimal(19,2),
  `total_cost` decimal(19,2),
  `begin_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_date` TIMESTAMP NULL DEFAULT NULL,
  PRIMARY KEY  (`pk_id`),
  UNIQUE KEY `unique_order_id__product_id__begin_date` (`order_id`,`product_id`,`begin_date`),
  KEY `key_order_id__begin_date` (`order_id`,`begin_date`),
  KEY `key_order_id` (`order_id`),
  KEY `key_begin_date` (`begin_date`),
  KEY `key_end_date` (`end_date`),
  CONSTRAINT `fk_product_sale__order` FOREIGN KEY (`order_id`) REFERENCES `order` (`object_id`),
  CONSTRAINT `fk_product_sale__product_id` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;