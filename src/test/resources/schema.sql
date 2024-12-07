create table if not exists `user`
(
    activate    boolean                not null,
    zip_code    int                    not null,
    created_at  timestamp              null,
    deleted_at  timestamp              null,
    id          bigint auto_increment
        primary key,
    modified_at timestamp              null,
    address1    varchar(255)           null,
    address2    varchar(255)           null,
    email       varchar(255)           not null unique,
    name        varchar(255)           not null,
    nick_name   varchar(255)           null,
    password    varchar(255)           not null,
    authority   varchar(10)            not null
);

create table if not exists coupon
(
    amount        int           null,
    discount_rate int           not null,
    expire_at     date          not null,
    id            bigint auto_increment
        primary key,
    name          varchar(255) not null,
    created_at    timestamp     null,
    modified_at   timestamp     null
);

create index if not exists coupon_idx
    on coupon (expire_at, id);

create table if not exists coupon_user
(
    is_available     boolean     not null,
    coupon_id        bigint      null,
    created_at       timestamp   null,
    id               bigint auto_increment
        primary key,
    modified_at      timestamp   null,
    point_history_id bigint      null unique,
    used_at          timestamp   null,
    user_id          bigint      null,
    constraint FKbvhy4yneyu9jrfluk0ixqrp2r
        foreign key (user_id) references `user` (id),
    constraint FKeyxuurk92vehhs6mj2nq40oni
        foreign key (coupon_id) references coupon (id)
);