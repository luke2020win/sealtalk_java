
/* 添加是否被禁字段字段 */
ALTER TABLE `users` ADD COLUMN `isDisable` int(10) unsigned NOT NULL DEFAULT '0' AFTER `stAccount`;

/* 登陆ip*/
ALTER TABLE `users` ADD COLUMN `ip` varchar(64) NOT NULL AFTER `stAccount`;