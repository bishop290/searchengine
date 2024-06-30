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
                     foreign key (`site_id`) references site(`id`));

--changeset Grigorii_Kuznetsov:3
create index `idx_path` ON `page` (`path`(40));

--changeset Grigorii_Kuznetsov:4
create table `lemma` (`id` int not null auto_increment,
                      `site_id` int not null,
                      `lemma` varchar(255) not null,
                      `frequency` int not null,
                      primary key (`id`));

--changeset Grigorii_Kuznetsov:5
create table `index` (`id` int not null auto_increment,
                      `page_id` int not null,
                      `lemma_id` int not null,
                      `rank` float not null,
                      primary key (`id`));

--changeset Grigorii_Kuznetsov:6
create index `idx_lemma` ON `lemma` (`lemma`(20));