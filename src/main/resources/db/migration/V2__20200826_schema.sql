
DROP TABLE IF EXISTS `backend_users`;
CREATE TABLE `backend_users`
(
    `id`             int(10) unsigned NOT NULL AUTO_INCREMENT,
    `accout`         varchar(32)      NOT NULL,
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
    UNIQUE KEY `backend_users` (`accout`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


DROP TABLE IF EXISTS `backend_system_config`;
CREATE TABLE `backend_system_config`
(
    `id`             int(10) unsigned NOT NULL AUTO_INCREMENT,
    `varName`        varchar(128)      NOT NULL,
    `varValue`       varchar(512)      NOT NULL,
    `varDes`         varchar(256)      NOT NULL DEFAULT '',
    `timestamp`      bigint(20)        NOT NULL DEFAULT '0',
    `createdAt`      datetime          NOT NULL,
    `updatedAt`      datetime          NOT NULL,
    `deletedAt`      datetime                  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `backend_system_config` (`varName`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;