
/* 添加是否被禁字段字段 */
ALTER TABLE `users` ADD COLUMN `is_disable` int(10) unsigned NOT NULL DEFAULT '0' AFTER `stAccount`;