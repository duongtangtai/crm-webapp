drop database crm_app;
create database crm_app;
USE crm_app;
CREATE TABLE IF NOT EXISTS roles (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(100),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_num varchar(100),	
    avatar VARCHAR(100),
    role_id INT,
    refresh_token varchar(255),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS status (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS jobs (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    start_date DATE,
    end_date DATE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS tasks (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    start_date DATE,
    end_date DATE,
    note varchar(255),
    user_id INT,
    job_id INT,
    status_id INT,
    PRIMARY KEY (id)
);
ALTER TABLE users ADD FOREIGN KEY (role_id) REFERENCES roles (id)  ON DELETE CASCADE;
ALTER TABLE tasks ADD FOREIGN KEY (user_id) REFERENCES users (id)  ON DELETE CASCADE;
ALTER TABLE tasks ADD FOREIGN KEY (job_id) REFERENCES jobs (id)  ON DELETE CASCADE;
ALTER TABLE tasks ADD FOREIGN KEY (status_id) REFERENCES status (id)  ON DELETE CASCADE;
-- ON DELETE SET NULL: if the PRIMARY KEY is DELETED, THE FOREIGN KEY will be SET NULL --
-- ON DELETE CASCADE: if the PRIMARY KEY is DELETED, THE FOREIGN KEY will be DELETED TOO --
-- CASCADE means like WATERFALL
insert into roles (name, description) values ("Admin", "Người toàn quyền quản lý");
insert into roles (name, description) values ("Leader", "Người quản lý nhân viên");
insert into roles (name, description) values ("Member", "Người thực hiện công việc");
insert into users (email, password, full_name, phone_num, role_id) 
values ("admin@gmail.com", "$2a$10$ucZpUlftGBfrXLbcCXf8TOUNVqj5A78BSpF592uzWBLfv./Riu/0a",
"Admin",123456789, 1); -- account: admin@gmail.com / password: admin
insert into users (email, password, full_name, phone_num, role_id) 
values ("duongtangtai@gmail.com", "$2a$10$duOA6gd74eKyr1j4dt4FEu7O/mzeNBuNUsxQfbmOVn33QZXryeYRe",
"Dương Tăng Tài",0706760754, 1); -- account: duongtangtai@gmail.com / password: 123
insert into users (email, password, full_name, phone_num, role_id) 
values ("mynhi0108@gmail.com", "$2a$10$Ah.24UNL3i5ZAwFglg5FbeKOHJI0t.X.AvPrKbDxhor1KPM3PV8Fq",
"Trần Mỹ Nhi",123456789, 2); -- account: mynhi0108@gmail.com / password: 456
insert into users (email, password, full_name, phone_num, role_id) 
values ("kimngan@gmail.com", "$2a$10$ip5sJb3c3EiYLG2QdJeKHeImnkXD7PnAXOEFVtIdvI.NqwkREhsxa",
"Dương Kim Ngân",789456123, 3); -- account: kimngan@gmail.com / password: 789
insert into jobs (name, start_date, end_date) 
values ("Dự án A", "2020-10-10","2020-12-12");
insert into jobs (name, start_date, end_date) 
values ("Dự án B", "2021-02-02","2022-04-04");
insert into jobs (name, start_date, end_date) 
values ("Dự án C", "2022-06-06","2022-08-08");
insert into status (name) values ("Chưa bắt đầu");
insert into status (name) values ("Đang thực hiện");
insert into status (name) values ("Đã hoàn thành");
insert into tasks (name, start_date, end_date, user_id, job_id, status_id, note)
values ("Công việc 1", "2020-12-10", "2020-12-20", 1, 1, 1, "Những việc cần làm? Các bước thực hiện?");
insert into tasks (name, start_date, end_date, user_id, job_id, status_id, note)
values ("Công việc 2", "2020-12-21", "2020-12-30", 2, 2, 2, "Những việc cần làm? Các bước thực hiện?");
insert into tasks (name, start_date, end_date, user_id, job_id, status_id, note)
values ("Công việc 4", "2021-01-10", "2021-01-20", 3, 3, 3, "Những việc cần làm? Các bước thực hiện?");
insert into tasks (name, start_date, end_date, user_id, job_id, status_id, note)
values ("Công việc 5", "2020-12-10", "2020-12-20", 1, 1, 1, "Những việc cần làm? Các bước thực hiện?");
insert into tasks (name, start_date, end_date, user_id, job_id, status_id, note)
values ("Công việc 6", "2020-12-21", "2020-12-30", 2, 2, 1, "Những việc cần làm? Các bước thực hiện?");
insert into tasks (name, start_date, end_date, user_id, job_id, status_id, note)
values ("Công việc 7", "2021-01-10", "2021-01-20", 3, 3, 2, "Những việc cần làm? Các bước thực hiện?");
insert into tasks (name, start_date, end_date, user_id, job_id, status_id, note)
values ("Công việc 8", "2021-01-10", "2021-01-20", 1, 3, 2, "Những việc cần làm? Các bước thực hiện?");
insert into tasks (name, start_date, end_date, user_id, job_id, status_id, note)
values ("Công việc 9", "2021-01-10", "2021-01-20", 1, 3, 3, "Những việc cần làm? Các bước thực hiện?");
insert into tasks (name, start_date, end_date, user_id, job_id, status_id, note)
values ("Công việc 10", "2021-01-10", "2021-01-20", 2, 3, 2, "Những việc cần làm? Các bước thực hiện?");
insert into tasks (name, start_date, end_date, user_id, job_id, status_id, note)
values ("Công việc 11", "2021-01-10", "2021-01-20", 4, 1, 1, "Những việc cần làm? Các bước thực hiện?");
insert into tasks (name, start_date, end_date, user_id, job_id, status_id, note)
values ("Công việc 12", "2021-01-10", "2021-01-20", 4, 2, 1, "Những việc cần làm? Các bước thực hiện?");
insert into tasks (name, start_date, end_date, user_id, job_id, status_id, note)
values ("Công việc 13", "2021-01-10", "2021-01-20", 4, 2, 2, "Những việc cần làm? Các bước thực hiện?");