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

-- STAGING

-- tạo bảng tạm cho nguồn cellphones
CREATE TABLE if not exists staging_head_phone
(
    id            INT AUTO_INCREMENT NOT NULL,
    product_id    VARCHAR(255)          NULL,
    name          VARCHAR(255)          NULL,
    brand         VARCHAR(255)          NULL,
    type          VARCHAR(255)          NULL,
    price         VARCHAR(255)          NULL,
    warranty_info VARCHAR(255)          NULL,
    feature       VARCHAR(255)          NULL,
    voice_control VARCHAR(255)          NULL,
    microphone    VARCHAR(255)          NULL,
    battery_life  VARCHAR(255)          NULL,
    dimensions    VARCHAR(255)          NULL,
    weight        VARCHAR(255)          NULL,
    compatibility VARCHAR(255)          NULL,
    created_at    VARCHAR(255)          NULL,
    CONSTRAINT pk_staging_head_phone PRIMARY KEY (id)
);

-- Tạo bảng staging để lưu dữ liệu từ bảng tạm
CREATE TABLE if not exists staging_head_phone_daily
(
    id            INT AUTO_INCREMENT NOT NULL,
    product_id    VARCHAR(255)          NULL,
    name          VARCHAR(255)          NULL,
    brand         VARCHAR(255)          NULL,
    type          VARCHAR(255)          NULL,
    price         DECIMAL               NULL,
    warranty_info VARCHAR(255)          NULL,
    feature       VARCHAR(255)          NULL,
    voice_control VARCHAR(255)          NULL,
    microphone    VARCHAR(255)          NULL,
    battery_life  VARCHAR(255)          NULL,
    dimensions    VARCHAR(255)          NULL,
    weight        VARCHAR(255)          NULL,
    compatibility VARCHAR(255)          NULL,
    created_at    datetime              NULL,
    CONSTRAINT pk_staging_head_phone_daily PRIMARY KEY (id)
);
