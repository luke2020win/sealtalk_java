
DROP TABLE IF EXISTS `version_update`;
CREATE TABLE `version_update`
(
    `id`             int(10)        unsigned NOT NULL AUTO_INCREMENT,
    `clientType`     char(32)       NOT NULL,
    `version`        char(32)       NOT NULL,
    `channel`        char(32)       NOT NULL DEFAULT 'official',
    `versionCode`    int(10)        unsigned NOT NULL DEFAULT '0',
    `content`        varchar(512)   NOT NULL,
    `url`            varchar(128)   NOT NULL,
    `isShowUpdate`   int(10)        unsigned NOT NULL DEFAULT '0',
    `isForce`        int(10)        unsigned NOT NULL DEFAULT '0',
    `isPlist`        int(10)        unsigned NOT NULL DEFAULT '0',
    `description`    varchar(512)   NOT NULL DEFAULT '暂无备注',
    `timestamp`      bigint(20)     NOT NULL DEFAULT '0',
    `createdAt`      datetime       NOT NULL,
    `updatedAt`      datetime       NOT NULL,
    `deletedAt`      datetime       DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `version_update_client_type_version_channel` (`clientType`, `versionCode`, channel)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
