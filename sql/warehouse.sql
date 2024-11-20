-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.0.30 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.1.0.6537
-- --------------------------------------------------------


-- Dumping database structure for products_db
CREATE DATABASE IF NOT EXISTS `products_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `products_db`;


/*
Vì đảm bảo quá trình load vào staging không bị lôi, đưa tất cả các cột của bảng tạm thành kiểu dữ liệu text
Sau đó khi load vào dbstaging sẽ tiến hành transform và cleaning dữ liệu 
Sau khi cleaning và transform dữ liệu sẽ được lưu vào staging
Khi bảng staging có dữ liệu, sẽ có 1 proc để load từ staging vào datawarehouse 
*/

-- WAREHOUSE


CREATE TABLE IF NOT EXISTS head_phone (
	`id` int NOT NULL AUTO_INCREMENT,
	`product_id` varchar(255) DEFAULT NULL,
	`name` varchar(255) DEFAULT NULL,
	`brand` varchar(255) DEFAULT NULL,
	`type` varchar(255) DEFAULT NULL,
	`price` decimal(10,2) DEFAULT NULL,
	`warranty_info` text,
	`feature` text,
	`voice_control` varchar(255) DEFAULT NULL,
	`microphone` varchar(255) DEFAULT NULL,
	`battery_life` varchar(255) DEFAULT NULL,
	`dimensions` varchar(255) DEFAULT NULL,
	`weight` varchar(255) DEFAULT NULL,
	`compatibility` varchar(255) DEFAULT NULL,
	`created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
	`isDelete` BOOLEAN DEFAULT FALSE, -- Cột kiểm tra trạng thái xóa
	`date_delete` DATE,                -- Ngày xóa
	`date_insert` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Ngày chèn
	`expired_date` DATE DEFAULT '9999-12-31', -- Ngày hết hạn mặc định
	PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS date_dim (
	date_sk INT PRIMARY KEY,
	full_date DATE NOT NULL,
	day_of_week VARCHAR(10) NOT NULL,   -- Thứ trong tuần (Monday, Tuesday...)
	calendar_month VARCHAR(10) NOT NULL, -- Tháng (January, February...)
	calendar_year INT NOT NULL,          -- Năm
	day_of_month INT NOT NULL,           -- Ngày trong tháng (1-31)
	day_of_year INT NOT NULL,            -- Ngày trong năm (1-365)
	week_of_year_sunday INT NOT NULL,    -- Số tuần theo ngày chủ nhật bắt đầu
	week_of_year_monday INT NOT NULL,    -- Số tuần theo ngày thứ hai bắt đầu
	holiday VARCHAR(15) NOT NULL,        -- Ngày lễ (Yes/No)
	day_type VARCHAR(10) NOT NULL        -- Loại ngày (Weekday/Weekend)
);


ALTER TABLE head_phone
	ADD COLUMN date_insert_fk int;
ALTER TABLE head_phone
    ADD CONSTRAINT fk_date_insert FOREIGN KEY (date_insert_fk) REFERENCES date_dim(date_sk);
