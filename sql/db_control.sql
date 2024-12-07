create database if not exists config_db;
use config_db;

CREATE TABLE if not exists ds_config (
    ds_id INT AUTO_INCREMENT PRIMARY KEY, # id
    ds_name VARCHAR(255),
    ds_crawl_config_url VARCHAR(255), # data crawling config url
    ds_crawl_save_location VARCHAR(255), # lưu ở đâu
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE if not exists process_tracker (
    process_id INT AUTO_INCREMENT PRIMARY KEY,
    process_name VARCHAR,
    process_status ENUM('READY', 'IN_PROGRESS', 'SUCCESS', 'FAILURE') NOT NULL,
    process_last_start_time DATETIME,
    process_last_end_time DATETIME,
    process_required INT UNIQUE REFERENCES process_tracker(process_id),
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE if not exists logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    log_process_id INT REFERENCES process_tracker(process_id),
    log_process_start TIMESTAMP,
    log_process_end TIMESTAMP,
    log_level INT,
    log_message VARCHAR,
    log_date DATE,
    deleted BOOLEAN DEFAULT FALSE
);


-- Dữ liệu mẫu

INSERT INTO ds_config (ds_name, ds_crawl_config_url, ds_crawl_save_location)
VALUES ('cellphones', 'crawl_conf/cellphones.json', 'crawl_conf/cellphones.csv');


INSERT INTO process_tracker(`process_id`, `deleted`, `process_last_end_time`, `process_name`, `process_last_start_time`, `process_status`, `process_required`) VALUES (1, b'0', '2024-12-04 23:12:48.000000', 'Crawl CellphoneS Data', '2024-12-04 23:12:47.000000', 'SUCCESS', NULL);
INSERT INTO process_tracker(`process_id`, `deleted`, `process_last_end_time`, `process_name`, `process_last_start_time`, `process_status`, `process_required`) VALUES (2, b'0', '2024-12-04 23:12:48.000000', 'Import CellphoneS data to CellphoneS Staging', '2024-12-04 23:12:48.000000', 'SUCCESS', 1);
INSERT INTO process_tracker(`process_id`, `deleted`, `process_last_end_time`, `process_name`, `process_last_start_time`, `process_status`, `process_required`) VALUES (3, b'0', '2024-12-04 23:12:48.000000', 'Transform CellphoneS Staging and Load to Global Staging', '2024-12-04 23:12:48.000000', 'SUCCESS', 2);
INSERT INTO process_tracker(`process_id`, `deleted`, `process_last_end_time`, `process_name`, `process_last_start_time`, `process_status`, `process_required`) VALUES (4, b'0', '2024-12-04 23:12:49.000000', 'Load From Staging To Warehouse', '2024-12-04 23:12:48.000000', 'SUCCESS', 3);
INSERT INTO process_tracker(`process_id`, `deleted`, `process_last_end_time`, `process_name`, `process_last_start_time`, `process_status`, `process_required`) VALUES (5, b'0', '2024-12-04 23:12:49.000000', 'Main', '2024-12-04 23:12:47.000000', 'SUCCESS', NULL);

