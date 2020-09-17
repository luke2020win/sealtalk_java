
/* 添加是否被禁字段字段 */
ALTER TABLE `users` ADD COLUMN `wxOpenId` varchar(128)  NOT NULL DEFAULT '' AFTER `rongCloudToken`;

/* 登陆ip*/
ALTER TABLE `users` ADD COLUMN `qqOpenId` varchar(128) NOT NULL DEFAULT '' AFTER `rongCloudToken`;