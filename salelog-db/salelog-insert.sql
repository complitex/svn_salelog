INSERT INTO `sequence` (`sequence_name`, `sequence_value`) VALUES
('order',1);

INSERT INTO `country` VALUES (1,1,NULL,NULL,now(),NULL,'ACTIVE',0,NULL);
INSERT INTO `country_attribute` VALUES (1,1,1,800,1,800,now(),NULL,'ACTIVE');
INSERT INTO `country_string_culture` VALUES (1,1,1,'УКРАИНА');

INSERT INTO `order_status` (`pk_id`, `code`, `name`) values (5,0,'пусто'),(1,1,'доставлен'),(2,2,'отказ'),(3,3,'недозвон'),(4,4,'особый');

DELETE FROM `usergroup` where `group_name`='EMPLOYEES_CHILD_VIEW';

-- Current database version
 INSERT INTO `update` (`version`) VALUE ('20130924_2_0.0.1');