
DROP TABLE IF EXISTS `group_mute_lists`;
CREATE TABLE `group_mute_lists`
(
    `id`              int(10) unsigned NOT NULL AUTO_INCREMENT,
    `groupId`         int(10) unsigned NOT NULL,
    `muteUserId`      int(10) unsigned NOT NULL,
    `muteNickname`    varchar(256)      NOT NULL,
    `mutePortraitUri` varchar(256)     NOT NULL DEFAULT '',
    `muteTime`        int(10)       NOT NULL DEFAULT '0',
    `operatorId`      int(10) unsigned          DEFAULT NULL,
    `operatorNickName`    varchar(256)               DEFAULT NULL,
    `createdAt`       datetime         NOT NULL,
    `updatedAt`       datetime         NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `group_mute_lists_groupId_muteUserId` (`groupId`, `muteUserId`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;