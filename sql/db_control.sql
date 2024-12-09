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
                                               process_name VARCHAR(255),
                                               process_status ENUM('READY', 'IN_PROGRESS', 'SUCCESS', 'FAILED') DEFAULT 'READY',
                                               process_last_start_time DATETIME,
                                               process_last_end_time DATETIME,
                                               process_required INT UNIQUE REFERENCES process_tracker(process_id),
                                               deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE if not exists logs (
                                    log_id INT AUTO_INCREMENT PRIMARY KEY,
                                    log_process_id INT REFERENCES process_tracker(process_id),
                                    log_process_start DATETIME,
                                    log_process_end DATETIME,
                                    log_level INT,
                                    log_message VARCHAR(1024),
                                    log_date DATE,
                                    deleted BOOLEAN DEFAULT FALSE
);


-- Dữ liệu mẫu

INSERT INTO ds_config (ds_name, ds_crawl_config_url, ds_crawl_save_location)
VALUES ('cellphones', 'crawl_conf/cellphones.json', 'crawl_conf/cellphones_ddmmyy_hhmm.csv');


INSERT INTO process_tracker(`process_id`, `deleted`, `process_last_end_time`, `process_name`, `process_last_start_time`, `process_status`, `process_required`) VALUES (1, b'0', NOW(), 'Crawl CellphoneS Data', NOW(), 'READY', NULL);
INSERT INTO process_tracker(`process_id`, `deleted`, `process_last_end_time`, `process_name`, `process_last_start_time`, `process_status`, `process_required`) VALUES (2, b'0', NOW(), 'Import CellphoneS data to CellphoneS Staging', NOW(), 'READY', 1);
INSERT INTO process_tracker(`process_id`, `deleted`, `process_last_end_time`, `process_name`, `process_last_start_time`, `process_status`, `process_required`) VALUES (3, b'0', NOW(), 'Transform CellphoneS Staging and Load to Global Staging', NOW(), 'READY', 2);
INSERT INTO process_tracker(`process_id`, `deleted`, `process_last_end_time`, `process_name`, `process_last_start_time`, `process_status`, `process_required`) VALUES (4, b'0', NOW(), 'Load From Staging To Warehouse', NOW(), 'READY', 3);
INSERT INTO process_tracker(`process_id`, `deleted`, `process_last_end_time`, `process_name`, `process_last_start_time`, `process_status`, `process_required`) VALUES (5, b'0', NOW(), 'Main', NOW(), 'READY', NULL);

