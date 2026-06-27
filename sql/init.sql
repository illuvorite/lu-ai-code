-- 创建数据库
CREATE DATABASE IF NOT EXISTS `lu-ai-code` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `lu-ai-code`;

-- 用户表
CREATE TABLE IF NOT EXISTS `user`
(
    `id`            BIGINT       NOT NULL COMMENT 'id' PRIMARY KEY,
    `userAccount`   VARCHAR(256) NOT NULL COMMENT '账号',
    `userPassword`  VARCHAR(512) NOT NULL COMMENT '密码',
    `userName`      VARCHAR(256) NULL COMMENT '用户昵称',
    `userAvatar`    VARCHAR(1024) NULL COMMENT '用户头像',
    `userProfile`   VARCHAR(512) NULL COMMENT '用户简介',
    `userRole`      VARCHAR(256) DEFAULT 'user' NOT NULL COMMENT '用户角色：user/admin/superadmin',
    `userEmail`     VARCHAR(256) NULL COMMENT '邮箱',
    `editTime`      DATETIME     DEFAULT CURRENT_TIMESTAMP NULL COMMENT '编辑时间',
    `createTime`    DATETIME     DEFAULT CURRENT_TIMESTAMP NULL COMMENT '创建时间',
    `updateTime`    DATETIME     DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`      TINYINT      DEFAULT 0 NOT NULL COMMENT '是否删除',
    `vipExpireTime` DATETIME     NULL COMMENT '会员过期时间',
    `vipCode`       VARCHAR(256) NULL COMMENT '会员兑换码',
    `vipNumber`     BIGINT       NULL COMMENT '会员编号',
    `shareCode`     VARCHAR(256) NULL COMMENT '分享码',
    `inviteUser`    BIGINT       NULL COMMENT '邀请用户 id',
    INDEX `idx_userAccount` (`userAccount`),
    INDEX `idx_userName` (`userName`),
    INDEX `idx_userEmail` (`userEmail`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = '用户';

-- 插入默认管理员账号（密码: admin123，加密方式 MD5(lu + 密码)）
INSERT INTO `user` (`id`, `userAccount`, `userPassword`, `userName`, `userRole`, `userEmail`)
VALUES (1, 'admin', '525d57fef3c2fa318bc3f61c787914d9', '管理员', 'admin', 'admin@example.com');
