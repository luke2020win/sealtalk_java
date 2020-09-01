
DROP TABLE IF EXISTS `backend_users`;
CREATE TABLE `backend_users`
(
    `id`             int(10) unsigned NOT NULL AUTO_INCREMENT,
    `account`         varchar(32)      NOT NULL,
    `roleType`       varchar(64)      NOT NULL,
    `passwordHash`   char(128)        NOT NULL,
    `passwordSalt`   char(128)        NOT NULL,
    `token`          varchar(256)     NOT NULL DEFAULT '',
    `portraitUri`    varchar(256)     NOT NULL DEFAULT '',
    `ip`             varchar(64)      NOT NULL,
    `timestamp`      bigint(20)       NOT NULL DEFAULT '0',
    `createdAt`      datetime         NOT NULL,
    `updatedAt`      datetime         NOT NULL,
    `deletedAt`      datetime         DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `backend_users` (`account`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

INSERT INTO  backend_users (id, account, roleType, passwordHash, passwordSalt, token, portraitUri, ip, timestamp, createdAt, updatedAt, deletedAt) VALUES (1, 'admin', '超级管理员', '9a3a19663c243818610cc0f5d4280d994423a4e7', '2883', 'k2TZdFBSp6N62pAxlp7GJLf4ugwPKSjQaxdxgfAT1KY=@4qqh.cn.rongnav.com;4qqh.cn.rongcfg.com', 'http://download.hotchatvip.com/default_hotchat_avatar.png', '112.211.5.24', NOW(), NOW(), NOW(), NOW());



DROP TABLE IF EXISTS `backend_system_config`;
CREATE TABLE `backend_system_config`
(
    `id`             int(10) unsigned NOT NULL AUTO_INCREMENT,
    `varName`        varchar(128)      NOT NULL,
    `varValue`       varchar(512)      NOT NULL,
    `varDes`         varchar(256)      NOT NULL DEFAULT '',
    `description`    varchar(512)      DEFAULT NULL,
    `timestamp`      bigint(20)        NOT NULL DEFAULT '0',
    `createdAt`      datetime          NOT NULL,
    `updatedAt`      datetime          NOT NULL,
    `deletedAt`      datetime                  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `backend_system_config` (`varName`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

INSERT INTO backend_system_config (id, varName, varValue, varDes, description, timestamp, createdAt, updatedAt, deletedAt) VALUE (1, 'discoveryH5Url','https://m.4xj70.com/index','H5地址', '星聊地址： https://m.4xj70.com/index', NOW(), NOW(), NOW(), NOW());
INSERT INTO backend_system_config (id, varName, varValue, varDes, description, timestamp, createdAt, updatedAt, deletedAt) VALUE (2, 'discoveryStatusColor','#d7363b', '发现页状态栏颜色', '白色：#f9f9f9 星际红：#d7363b', NOW(), NOW(), NOW(), NOW());
insert backend_system_config (id, varName, varValue, varDes, description, timestamp, createdAt, updatedAt, deletedAt) VALUE (3, 'appDownloadUrl','http://web.hotchatvip.com/download','APP下载地址', '暂无备注', NOW(), NOW(), NOW(), NOW());
INSERT INTO backend_system_config (id, varName, varValue, varDes, description, timestamp, createdAt, updatedAt, deletedAt) VALUE (4, 'isShowDiscoveryH5','true','是否现实发现页H5', '发现页面显示H5：true 不显示：false', NOW(), NOW(), NOW(), NOW());
