--
-- Create company database
-- as of 2018-07-03
--
create database if not exists company default charset = utf8;

--
-- Create user and grant privileges
--
CREATE USER 'lucas'@'localhost';
GRANT ALL PRIVILEGES ON company.* To 'lucas'@'localhost' IDENTIFIED BY 'lucas';

--
-- Table structure for table 'admin'
--
use company;
drop table if exists admin;
create table admin (
	adminId int(11) not null,
	userName varchar(255) default null,
	password varchar(255) default null,
	primary key (adminId)
) engine=InnoDB default charset=utf8;

--
-- Adding data to table 'admin'
--
lock tables admin write;
insert into admin (adminId, userName, password) values (1, 'A', 'a');
insert into admin (adminId, userName, password) values (2, 'B', 'b');
insert into admin (adminId, userName, password) values (3, 'C', 'c');
insert into admin (adminId, userName, password) values (4, 'D', 'd');
insert into admin (adminId, userName, password) values (5, 'E', 'e');
unlock tables;