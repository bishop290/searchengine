--liquibase formatted sql

--changeset Grigorii_Kuznetsov:1
create table `site` (`id` int not null auto_increment,
                     `status` enum('INDEXING', 'INDEXED', 'FAILED') not null,
                     `status_time` datetime not null,
                     `last_error` text,
                     `url` varchar(255) not null,
                     `name` varchar(255) not null,
                     primary key (`id`));

--changeset Grigorii_Kuznetsov:2
create table `page` (`id` int not null auto_increment,
                     `site_id` int not null,
                     `path` text not null,
                     `code` int not null,
                     `content` mediumtext not null,
                     primary key (`id`),
                     foreign key (site_id) references site(id));

--changeset Grigorii_Kuznetsov:3
create index `idx_path` ON `page` (`path`(20));

