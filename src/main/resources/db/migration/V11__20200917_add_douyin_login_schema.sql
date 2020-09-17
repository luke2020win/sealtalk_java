
/* 添加是否被禁字段字段 */
ALTER TABLE `users` ADD COLUMN `dyOpenId` varchar(128)  NOT NULL DEFAULT '' AFTER `rongCloudToken`;