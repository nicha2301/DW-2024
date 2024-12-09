-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.0.30 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.1.0.6537
-- --------------------------------------------------------


-- Dumping database structure for products_db
CREATE DATABASE IF NOT EXISTS `products_db` ;
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
-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.0.30 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.1.0.6537
-- --------------------------------------------------------


-- Dumping database structure for products_db
CREATE DATABASE IF NOT EXISTS `products_db`;
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
/*B1: Xử lý dữ liệu bị xóa
	+ Tìm các dòng dữ liệu ở warehouse hiện tại và tìm trong cleaned_data xem thử có không=> đổi
B2: insert các dòng có productId mới vào trước
B3: Xử lý dữ liệu bị trùng
	+ Tìm đến các dòng có productId giống nhau, so sánh các cột
	+ Nếu khác thì lưu vào 1 bảng temp_update_id
	+ Tìm tới các productId có trong bảng temp_update_id và chỉnh sửa các cột giá trị isDelete = True, expried_date = NOW(), date_delete = NOW()
	+ Insert từ cleaned_data các dòng có id trong temp_update_id vào
*/
use products_db;

drop procedure if exists load_data_to_warehouse;
DELIMITER $$

CREATE PROCEDURE load_data_to_warehouse()
BEGIN
    DECLARE curr_date DATE;
    SET curr_date = CURDATE();

    -- B1: Xử lý dữ liệu bị xóa
    UPDATE head_phone AS wh
        LEFT JOIN staging_head_phone_daily AS stg
        ON wh.product_id = stg.product_id
    SET wh.isDelete = TRUE,
        wh.expired_date = curr_date,
        wh.date_delete = curr_date
    WHERE stg.product_id IS NULL AND wh.isDelete = FALSE;

-- B2: Insert các dòng mới (product_id chưa tồn tại trong warehouse)
    INSERT INTO head_phone (
        product_id, name, brand, type, price, warranty_info,
        feature, voice_control, microphone, battery_life,
        dimensions, weight, compatibility, created_at, date_insert
    )
    SELECT
        stg.product_id, stg.name, stg.brand, stg.type, stg.price, stg.warranty_info,
        stg.feature, stg.voice_control, stg.microphone, stg.battery_life,
        stg.dimensions, stg.weight, stg.compatibility, stg.created_at, NOW()
    FROM staging_head_phone_daily AS stg
             LEFT JOIN head_phone AS wh
                       ON stg.product_id = wh.product_id
    WHERE wh.product_id IS NULL;

-- B3: Xử lý dữ liệu bị trùng lặp
    CREATE TEMPORARY TABLE temp_update_id (
                                              product_id VARCHAR(255) NOT NULL
    );

    -- Tìm các product_id bị trùng lặp và lưu vào bảng tạm
    INSERT INTO temp_update_id (product_id)
    SELECT stg.product_id
    FROM staging_head_phone_daily AS stg
             INNER JOIN head_phone AS wh
                        ON stg.product_id = wh.product_id
    WHERE wh.isDelete = FALSE AND(
        stg.name <> wh.name OR
        stg.brand <> wh.brand OR
        stg.type <> wh.type OR
        stg.price <> wh.price OR
        stg.warranty_info <> wh.warranty_info OR
        stg.feature <> wh.feature OR
        stg.voice_control <> wh.voice_control OR
        stg.microphone <> wh.microphone OR
        stg.battery_life <> wh.battery_life OR
        stg.dimensions <> wh.dimensions OR
        stg.weight <> wh.weight OR
        stg.compatibility <> wh.compatibility
        );

    -- Tìm tới các productId có trong bảng temp_update_id và chỉnh sửa các cột giá trị isDelete = True, expried_date = NOW(), date_delete = NOW()
-- Kiểm tra dữ liệu trong temp_update_id
    IF (SELECT COUNT(*) FROM temp_update_id) > 0 THEN
        -- Chỉ thực hiện UPDATE nếu temp_update_id có dữ liệu
        UPDATE head_phone AS wh
            INNER JOIN temp_update_id AS temp
            ON wh.product_id = temp.product_id
        SET wh.isDelete = TRUE,
            wh.expired_date = curr_date,
            wh.date_delete = curr_date;
    END IF;

-- Chèn các dòng cập nhật từ staging vào warehouse
    INSERT INTO head_phone (
        product_id, name, brand, type, price, warranty_info,
        feature, voice_control, microphone, battery_life,
        dimensions, weight, compatibility, created_at, date_insert
    )
    SELECT
        stg.product_id, stg.name, stg.brand, stg.type, stg.price, stg.warranty_info,
        stg.feature, stg.voice_control, stg.microphone, stg.battery_life,
        stg.dimensions, stg.weight, stg.compatibility, stg.created_at, NOW()
    FROM staging_head_phone_daily AS stg
             INNER JOIN temp_update_id AS temp
                        ON stg.product_id = temp.product_id;

-- Xóa bảng tạm
    DROP TEMPORARY TABLE IF EXISTS temp_update_id;
END$$

DELIMITER ;
DELIMITER $$

CREATE PROCEDURE transform_and_load_staging_data()
BEGIN
    -- Bước 1: Xóa dữ liệu cũ trong staging_head_phone_daily (nếu cần)
    DELETE FROM staging_head_phone_daily;

-- Bước 2: Transform và chèn dữ liệu từ staging_head_phone vào staging_head_phone_daily
    INSERT INTO staging_head_phone_daily (
        product_id, NAME, brand, price, type, battery_life, compatibility, dimensions,
        feature, microphone, voice_control, warranty_info, weight, created_at
    )
    SELECT
        product_id,
        name,
        brand,
        -- Xử lý cột price
        CASE
            WHEN price = '0' THEN 0 -- Nếu giá là 0
            WHEN price REGEXP '^[0-9,]+$' THEN CAST(REPLACE(price, ',', '') AS DECIMAL(15, 2)) -- Loại bỏ dấu phẩy và chuyển thành số
            WHEN price REGEXP '^[0-9]+\\.[0-9]+E[+-][0-9]+$' THEN CAST(price AS DECIMAL(15, 2)) -- Dạng khoa học
            WHEN price REGEXP '^[0-9]+E[+-][0-9]+$' THEN CAST(price AS DECIMAL(15, 2)) -- Dạng exponential không chứa dấu chấm
            ELSE NULL -- Trường hợp không hợp lệ
            END AS price,
        -- Xử lý cột type
        CASE
            WHEN type IN ('-', '', 'Bàn') THEN
                CASE
                    WHEN name LIKE '%không dây%' OR name LIKE '%Bluetooth%' OR name LIKE '%nhét tai%'
                        OR name LIKE '%chụp tai%' OR name LIKE '%TWS%' THEN 'Tai nghe Bluetooth'
                    ELSE 'Tai nghe có dây'
                    END
            ELSE type
            END AS type,
        -- Xử lý cột battery_life
        CASE
            WHEN battery_life IS NULL OR battery_life = '' THEN 'Thông tin không có'
            ELSE REPLACE(REGEXP_REPLACE(battery_life, '<[^>]*>', ' '), '\n', ', ')
            END AS battery_life,
        -- Xử lý cột compatibility
        CASE
            WHEN compatibility IS NULL OR compatibility = '' THEN 'Hỗ trợ hầu hết các thiết bị'
            ELSE REPLACE(REGEXP_REPLACE(compatibility, '<[^>]*>', ''), '\n', ', ')
            END AS compatibility,
        -- Xử lý cột dimensions
        CASE
            WHEN dimensions IS NULL OR dimensions = '' THEN 'Kích thước không xác định'
            ELSE REPLACE(REGEXP_REPLACE(dimensions, '<[^>]*>', ''), '\n', ', ')
            END AS dimensions,
        -- Xử lý cột feature
        CASE
            WHEN feature IS NULL OR feature = '' THEN 'Nghe nhạc'
            ELSE REGEXP_REPLACE(feature, '<[^>]*>', '')
            END AS feature,
        -- Xử lý cột microphone
        CASE
            WHEN microphone IS NULL OR microphone = '' THEN 'Không'
            ELSE REGEXP_REPLACE(microphone, '<[^>]*>', ' ')
            END AS microphone,
        -- Xử lý cột voice_control
        CASE
            WHEN voice_control IS NULL OR voice_control = '' THEN 'Không hỗ trợ'
            ELSE REGEXP_REPLACE(voice_control, '<[^>]*>', '')
            END AS voice_control,
        -- Xử lý cột warranty_info
        CASE
            WHEN warranty_info IS NULL OR warranty_info = '' THEN 'Bảo hành 1 đổi 1 trong 12 tháng.'
            ELSE REGEXP_REPLACE(warranty_info, '<[^>]*>', '')
            END AS warranty_info,
        -- Xử lý cột weight
        CASE
            WHEN weight IS NULL OR weight = '' THEN '500g'
            ELSE REGEXP_REPLACE(weight, '<[^>]*>', '')
            END AS weight,
        -- Xử lý cột created_at
        CASE
            WHEN created_at IS NULL OR created_at = '' THEN NULL -- Nếu dữ liệu rỗng, trả về NULL
            ELSE STR_TO_DATE(SUBSTRING_INDEX(created_at, '.', 1), '%Y-%m-%dT%H:%i:%s') -- Chuyển đổi sang DATETIME
            END AS created_at
    FROM staging_head_phone;

-- Kiểm tra và ghi log nếu cần
    IF (SELECT COUNT(*) FROM staging_head_phone_daily) = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Dữ liệu không được chèn vào staging_head_phone_daily. Kiểm tra lại quy trình.';
    END IF;
END$$

DELIMITER ;
