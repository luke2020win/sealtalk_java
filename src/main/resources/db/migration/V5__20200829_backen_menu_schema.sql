
/* 增加变量备注*/
ALTER TABLE `backend_system_config` ADD COLUMN `description` varchar(512) DEFAULT NULL AFTER `varDes`;