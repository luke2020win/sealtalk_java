
/* 客户端类型 */
ALTER TABLE `users` ADD COLUMN `clientType`  char(64) DEFAULT NULL AFTER `gender`;

/* chennel*/
ALTER TABLE `users` ADD COLUMN `channel` char(64) DEFAULT NULL AFTER `gender`;
