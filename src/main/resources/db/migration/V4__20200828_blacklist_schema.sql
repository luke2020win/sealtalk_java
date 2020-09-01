
# 创建管理后台ip白单
DROP TABLE IF EXISTS `backend_ip_white`;
CREATE TABLE `backend_ip_white`
(
    `id`             int(10) unsigned  NOT NULL AUTO_INCREMENT,
    `ip`             varchar(64)       NOT NULL DEFAULT '0.0.0.0',
    `description`    varchar(512)      NOT NULL DEFAULT '',
    `timestamp`      bigint(20)        NOT NULL DEFAULT '0',
    `createdAt`      datetime          NOT NULL,
    `updatedAt`      datetime          NOT NULL,
    `deletedAt`      datetime          DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `backend_ip_white` (`ip`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

INSERT INTO backend_ip_white(id, ip, description, timestamp, createdAt, updatedAt, deletedAt) VALUES (1, '112.211.5.24', '暂无备注', NOW(), NOW(), NOW(), NOW());

# 创建注册用户ip黑名单
DROP TABLE IF EXISTS `user_ip_black`;
CREATE TABLE `user_ip_black`
(
    `id`             int(10) unsigned  NOT NULL AUTO_INCREMENT,
    `ip`             varchar(64)       NOT NULL DEFAULT '0.0.0.0',
    `description`    varchar(512)      NOT NULL,
    `timestamp`      bigint(20)        NOT NULL DEFAULT '0',
    `createdAt`      datetime          NOT NULL,
    `updatedAt`      datetime          NOT NULL,
    `deletedAt`      datetime          DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `user_ip_black` (`ip`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


# 创建注册用户黑名单
DROP TABLE IF EXISTS `user_black`;
CREATE TABLE `user_black`
(
    `id`             int(10) unsigned  NOT NULL AUTO_INCREMENT,
    `region`         varchar(5)       NOT NULL,
    `phone`          varchar(11)      NOT NULL,
    `nickname`       varchar(32)      NOT NULL,
    `portraitUri`    varchar(256)     NOT NULL DEFAULT '',
    `stAccount`      varchar(32)      NOT NULL DEFAULT '',
    `timestamp`      bigint(20)       NOT NULL DEFAULT '0',
    `createdAt`      datetime         NOT NULL,
    `updatedAt`      datetime         NOT NULL,
    `deletedAt`      datetime         DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `user_black_region_phone` (`region`, `phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
