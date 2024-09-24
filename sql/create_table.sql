-- auto-generated definition
create table user
(
    id             bigint auto_increment comment 'id'
        primary key,
    userAccount    varchar(20)                            not null comment '账号',
    userPassword   varchar(60)                            null comment '密码',
    unionId        varchar(256)                           null comment '微信开放平台id',
    githubId       bigint                                 null comment 'github用户Id',
    githubUserName varchar(30)                            null comment 'github用户名',
    userPhone      varchar(20)                            null comment '用户手机号(后期允许拓展区号和国际号码）',
    userEmail      varchar(254)                           null comment '用户邮箱',
    mpOpenId       varchar(256)                           null comment '公众号openId',
    userName       varchar(30)                            null comment '用户昵称',
    userGender     int                                    not null comment '用户性别',
    userAvatar     varchar(1024)                          null comment '用户头像',
    userProfile    varchar(512)                           null comment '用户简介',
    userRole       varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete       tinyint      default 0                 not null comment '是否删除',
    constraint user_pk
        unique (userAccount),
    constraint user_pk_2
        unique (userEmail)
)
    comment '用户' collate = utf8mb4_unicode_ci;

create index idx_unionId
    on user (unionId);

create index user_userAccount_index
    on user (userAccount);

create index user_userEmail_index
    on user (userEmail);

