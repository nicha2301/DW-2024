create database if not exists config_db;
use config_db;

CREATE TABLE if not exists ds_config (
    ds_id INT AUTO_INCREMENT PRIMARY KEY, # id
    ds_name VARCHAR(255),
    ds_crawl_config_url VARCHAR(255), # data crawling config url
    ds_crawl_save_location VARCHAR(255), # lưu ở đâu
    tbl_staging VARCHAR(255), # bảng để map bên staging
    tbl_warehouse VARCHAR(255), # bảng để map tới warehouse
    fields_staging VARCHAR(255), # các trường để map bên staging
    fields_staging_daily VARCHAR(255) # các trường để map bên staging real
);

CREATE TABLE if not exists process_tracker (
    process_id INT AUTO_INCREMENT PRIMARY KEY,
    process_name VARCHAR,
    process_status ENUM('READY', 'IN_PROGRESS', 'SUCCESS', 'FAILURE') NOT NULL,
    process_last_start_time DATETIME,
    process_last_end_time DATETIME
);

CREATE TABLE if not exists logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    log_process_id INT,
    log_process_start TIMESTAMP,
    log_process_end TIMESTAMP,
    log_level INT,
    log_message VARCHAR,
    log_date DATE
);


-- Dữ liệu mẫu

INSERT INTO ds_config (ds_name, ds_crawl_config_url, ds_crawl_save_location, tbl_staging, fields_staging, fields_staging_daily)
VALUES
    ('cellphones', 'crawl_conf/cellphones.json', 'crawl_conf/cellphones.csv', 'staging_head_phone', 'product_id,name,brand,type,price,warranty_info,feature,voice_control,microphone,battery_life,dimensions,weight,compatibility', 'product_id,name,brand,type,price,warranty_info,feature,voice_control,microphone,battery_life,dimensions,weight,compatibility');


#INSERT INTO process_tracker (process_name, process_status)
#VALUES ("Crawling Data", "C_RE");
