-- 创建库
create database if not exists lingmo_studio;
use lingmo_studio;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    -- 确保 userAccount（用户账号）字段在整个表中是唯一的，不允许重复
    UNIQUE KEY uk_userAccount (userAccount),
    -- 为 userName（用户昵称）字段创建索引，加速查询
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 初始化测试数据（密码是 12345678，MD5 加密 + 盐值）
INSERT INTO user (id, userAccount, userPassword, userName, userAvatar, userProfile, userRole) VALUES
(1, 'admin', '59c624f637264a1be899381c7bac45f0', '管理员', 'https://pic1.zhimg.com/v2-4e7f93275a2f7100bebdd93ae1e4188c_b.jpg', '系统管理员', 'admin'),
(2, 'user', '59c624f637264a1be899381c7bac45f0', '普通用户', 'https://pic2.zhimg.com/v2-2fe100236cfafb9eb49862fc88dc6555_r.jpg', '普通用户', 'user'),
(3, 'test', '59c624f637264a1be899381c7bac45f0', '测试账号', 'https://pic4.zhimg.com/v2-0dbc479a4fa0f130a1622e36b9f43057_r.jpg', '测试账号', 'user');
