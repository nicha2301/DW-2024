/*
 Navicat Premium Data Transfer

 Source Server         : localhost mysql
 Source Server Type    : MySQL
 Source Server Version : 100428
 Source Host           : localhost:3306
 Source Schema         : config_db

 Target Server Type    : MySQL
 Target Server Version : 100428
 File Encoding         : 65001

 Date: 28/11/2024 17:36:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ds_config
-- ----------------------------
DROP TABLE IF EXISTS `ds_config`;
CREATE TABLE `ds_config`  (
  `ds_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ds_crawl_config_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `ds_crawl_save_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `tbl_staging_daily` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `tbl_warehouse_daily` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `ds_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `fields_staging` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `tbl_staging` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `fields_warehouse` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`ds_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ds_config
-- ----------------------------
INSERT INTO `ds_config` VALUES (1, 'crawl_conf/cellphones.json', 'crawl_conf/cellphones.csv', 'staging_head_phone_daily', 'warehouse_records', b'0', 'CellphoneS', NULL, 'staging_head_phone', NULL);

-- ----------------------------
-- Table structure for logs
-- ----------------------------
DROP TABLE IF EXISTS `logs`;
CREATE TABLE `logs`  (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `log_date` date NULL DEFAULT NULL,
  `deleted` bit(1) NOT NULL,
  `log_level` tinyint(4) NULL DEFAULT NULL,
  `log_message` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `log_process_end` datetime(6) NULL DEFAULT NULL,
  `log_process_start` datetime(6) NULL DEFAULT NULL,
  `log_process_id` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `FKbg2nihldqbo6uxi6hr5wp4gw1`(`log_process_id`) USING BTREE,
  CONSTRAINT `FKbg2nihldqbo6uxi6hr5wp4gw1` FOREIGN KEY (`log_process_id`) REFERENCES `process_tracker` (`process_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 87 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of logs
-- ----------------------------
INSERT INTO `logs` VALUES (1, '2024-11-28', b'0', 1, 'ETL process begins', '2024-11-28 16:08:10.000000', '2024-11-28 16:08:10.000000', 3);
INSERT INTO `logs` VALUES (2, '2024-11-28', b'0', 1, 'Getting all data source config to crawl.', '2024-11-28 16:08:10.000000', '2024-11-28 16:08:10.000000', 1);
INSERT INTO `logs` VALUES (3, '2024-11-28', b'0', 1, 'DataSource: DataSourceConfig(id=1, name=CellphoneS, crawlConfigURL=crawl_conf/cellphones.json, crawlSaveLocation=crawl_conf/cellphones.csv, stagingTableName=staging_head_phone, dailyStagingTableName=staging_head_phone_daily, dailyWarehouseTableName=warehouse_records, stagingFields=null, warehouseFields=null, deleted=false)', '2024-11-28 16:08:10.000000', '2024-11-28 16:08:10.000000', 1);
INSERT INTO `logs` VALUES (4, '2024-11-28', b'0', 3, 'ETL process ended with error JSONObject[\"product\"] not found.', '2024-11-28 16:08:14.000000', '2024-11-28 16:08:10.000000', 3);
INSERT INTO `logs` VALUES (5, '2024-11-28', b'0', 1, 'ETL process begins', '2024-11-28 16:09:11.000000', '2024-11-28 16:09:11.000000', 3);
INSERT INTO `logs` VALUES (6, '2024-11-28', b'0', 3, 'Có tiến trình khác đang chạy cùng crawl này, hủy.', '2024-11-28 16:09:11.000000', '2024-11-28 16:09:11.000000', 1);
INSERT INTO `logs` VALUES (7, '2024-11-28', b'0', 1, 'Starting data loading to warehouse', '2024-11-28 16:09:11.000000', '2024-11-28 16:09:11.000000', 2);
INSERT INTO `logs` VALUES (8, '2024-11-28', b'0', 3, 'Error loading data to warehouse: StatementCallback; bad SQL grammar [CALL load_data_to_warehouse()]', '2024-11-28 16:09:11.000000', '2024-11-28 16:09:11.000000', 2);
INSERT INTO `logs` VALUES (9, '2024-11-28', b'0', 1, 'ETL process ended successfully', '2024-11-28 16:09:11.000000', '2024-11-28 16:09:11.000000', 3);
INSERT INTO `logs` VALUES (10, '2024-11-28', b'0', 1, 'ETL process begins', '2024-11-28 16:21:04.000000', '2024-11-28 16:21:04.000000', 3);
INSERT INTO `logs` VALUES (11, '2024-11-28', b'0', 1, 'Getting all data source config to crawl.', '2024-11-28 16:21:04.000000', '2024-11-28 16:21:04.000000', 1);
INSERT INTO `logs` VALUES (12, '2024-11-28', b'0', 1, 'DataSource: DataSourceConfig(id=1, name=CellphoneS, crawlConfigURL=crawl_conf/cellphones.json, crawlSaveLocation=crawl_conf/cellphones.csv, stagingTableName=staging_head_phone, dailyStagingTableName=staging_head_phone_daily, dailyWarehouseTableName=warehouse_records, stagingFields=null, warehouseFields=null, deleted=false)', '2024-11-28 16:21:04.000000', '2024-11-28 16:21:04.000000', 1);
INSERT INTO `logs` VALUES (13, '2024-11-28', b'0', 1, 'Success.', '2024-11-28 16:21:07.000000', '2024-11-28 16:21:04.000000', 1);
INSERT INTO `logs` VALUES (14, '2024-11-28', b'0', 1, 'Starting data loading to warehouse', '2024-11-28 16:21:07.000000', '2024-11-28 16:21:07.000000', 2);
INSERT INTO `logs` VALUES (15, '2024-11-28', b'0', 3, 'Error loading data to warehouse: StatementCallback; bad SQL grammar [CALL load_data_to_warehouse()]', '2024-11-28 16:21:07.000000', '2024-11-28 16:21:07.000000', 2);
INSERT INTO `logs` VALUES (16, '2024-11-28', b'0', 1, 'ETL process ended successfully', '2024-11-28 16:21:07.000000', '2024-11-28 16:21:04.000000', 3);
INSERT INTO `logs` VALUES (17, '2024-11-28', b'0', 1, 'ETL process begins', '2024-11-28 17:07:34.000000', '2024-11-28 17:07:34.000000', 3);
INSERT INTO `logs` VALUES (18, '2024-11-28', b'0', 1, 'Getting all data source config to crawl.', '2024-11-28 17:07:34.000000', '2024-11-28 17:07:34.000000', 1);
INSERT INTO `logs` VALUES (19, '2024-11-28', b'0', 1, 'DataSource: DataSourceConfig(id=1, name=CellphoneS, crawlConfigURL=crawl_conf/cellphones.json, crawlSaveLocation=crawl_conf/cellphones.csv, stagingTableName=staging_head_phone, dailyStagingTableName=staging_head_phone_daily, dailyWarehouseTableName=warehouse_records, stagingFields=null, warehouseFields=null, deleted=false)', '2024-11-28 17:07:34.000000', '2024-11-28 17:07:34.000000', 1);
INSERT INTO `logs` VALUES (20, '2024-11-28', b'0', 1, 'Success.', '2024-11-28 17:07:37.000000', '2024-11-28 17:07:34.000000', 1);
INSERT INTO `logs` VALUES (21, '2024-11-28', b'0', 1, 'Starting data loading to warehouse', '2024-11-28 17:07:37.000000', '2024-11-28 17:07:37.000000', 2);
INSERT INTO `logs` VALUES (22, '2024-11-28', b'0', 3, 'Error loading data to warehouse: StatementCallback; bad SQL grammar [CALL load_data_to_warehouse()]', '2024-11-28 17:07:37.000000', '2024-11-28 17:07:37.000000', 2);
INSERT INTO `logs` VALUES (23, '2024-11-28', b'0', 1, 'ETL process ended successfully', '2024-11-28 17:07:37.000000', '2024-11-28 17:07:34.000000', 3);
INSERT INTO `logs` VALUES (24, '2024-11-28', b'0', 1, 'ETL process begins', '2024-11-28 17:09:50.000000', '2024-11-28 17:09:50.000000', 3);
INSERT INTO `logs` VALUES (25, '2024-11-28', b'0', 1, 'Getting all data source config to crawl.', '2024-11-28 17:09:50.000000', '2024-11-28 17:09:50.000000', 1);
INSERT INTO `logs` VALUES (26, '2024-11-28', b'0', 1, 'DataSource: DataSourceConfig(id=1, name=CellphoneS, crawlConfigURL=crawl_conf/cellphones.json, crawlSaveLocation=crawl_conf/cellphones.csv, stagingTableName=staging_head_phone, dailyStagingTableName=staging_head_phone_daily, dailyWarehouseTableName=warehouse_records, stagingFields=null, warehouseFields=null, deleted=false)', '2024-11-28 17:09:50.000000', '2024-11-28 17:09:50.000000', 1);
INSERT INTO `logs` VALUES (27, '2024-11-28', b'0', 1, 'Success.', '2024-11-28 17:09:52.000000', '2024-11-28 17:09:50.000000', 1);
INSERT INTO `logs` VALUES (28, '2024-11-28', b'0', 1, 'Starting data loading to warehouse', '2024-11-28 17:09:52.000000', '2024-11-28 17:09:52.000000', 2);
INSERT INTO `logs` VALUES (29, '2024-11-28', b'0', 3, 'Error loading data to warehouse: StatementCallback; bad SQL grammar [CALL load_data_to_warehouse()]', '2024-11-28 17:09:52.000000', '2024-11-28 17:09:52.000000', 2);
INSERT INTO `logs` VALUES (30, '2024-11-28', b'0', 1, 'ETL process ended successfully', '2024-11-28 17:09:52.000000', '2024-11-28 17:09:50.000000', 3);
INSERT INTO `logs` VALUES (31, '2024-11-28', b'0', 1, 'ETL process begins', '2024-11-28 17:10:22.000000', '2024-11-28 17:10:22.000000', 3);
INSERT INTO `logs` VALUES (32, '2024-11-28', b'0', 1, 'Getting all data source config to crawl.', '2024-11-28 17:10:22.000000', '2024-11-28 17:10:22.000000', 1);
INSERT INTO `logs` VALUES (33, '2024-11-28', b'0', 1, 'DataSource: DataSourceConfig(id=1, name=CellphoneS, crawlConfigURL=crawl_conf/cellphones.json, crawlSaveLocation=crawl_conf/cellphones.csv, stagingTableName=staging_head_phone, dailyStagingTableName=staging_head_phone_daily, dailyWarehouseTableName=warehouse_records, stagingFields=null, warehouseFields=null, deleted=false)', '2024-11-28 17:10:22.000000', '2024-11-28 17:10:22.000000', 1);
INSERT INTO `logs` VALUES (34, '2024-11-28', b'0', 1, 'Success.', '2024-11-28 17:10:24.000000', '2024-11-28 17:10:22.000000', 1);
INSERT INTO `logs` VALUES (35, '2024-11-28', b'0', 1, 'Starting data loading to warehouse', '2024-11-28 17:10:24.000000', '2024-11-28 17:10:24.000000', 2);
INSERT INTO `logs` VALUES (36, '2024-11-28', b'0', 3, 'Error loading data to warehouse: StatementCallback; bad SQL grammar [CALL load_data_to_warehouse()]', '2024-11-28 17:10:25.000000', '2024-11-28 17:10:25.000000', 2);
INSERT INTO `logs` VALUES (37, '2024-11-28', b'0', 1, 'ETL process ended successfully', '2024-11-28 17:10:25.000000', '2024-11-28 17:10:22.000000', 3);
INSERT INTO `logs` VALUES (38, '2024-11-28', b'0', 1, 'ETL process begins', '2024-11-28 17:17:14.000000', '2024-11-28 17:17:14.000000', 3);
INSERT INTO `logs` VALUES (39, '2024-11-28', b'0', 1, 'Getting all data source config to crawl.', '2024-11-28 17:17:14.000000', '2024-11-28 17:17:14.000000', 1);
INSERT INTO `logs` VALUES (40, '2024-11-28', b'0', 1, 'DataSource: DataSourceConfig(id=1, name=CellphoneS, crawlConfigURL=crawl_conf/cellphones.json, crawlSaveLocation=crawl_conf/cellphones.csv, stagingTableName=staging_head_phone, dailyStagingTableName=staging_head_phone_daily, dailyWarehouseTableName=warehouse_records, stagingFields=null, warehouseFields=null, deleted=false)', '2024-11-28 17:17:14.000000', '2024-11-28 17:17:14.000000', 1);
INSERT INTO `logs` VALUES (41, '2024-11-28', b'0', 1, 'Success.', '2024-11-28 17:17:17.000000', '2024-11-28 17:17:14.000000', 1);
INSERT INTO `logs` VALUES (42, '2024-11-28', b'0', 1, 'Starting data loading to warehouse', '2024-11-28 17:17:17.000000', '2024-11-28 17:17:17.000000', 2);
INSERT INTO `logs` VALUES (43, '2024-11-28', b'0', 3, 'Error loading data to warehouse: StatementCallback; bad SQL grammar [CALL load_data_to_warehouse()]', '2024-11-28 17:17:17.000000', '2024-11-28 17:17:17.000000', 2);
INSERT INTO `logs` VALUES (44, '2024-11-28', b'0', 1, 'ETL process ended successfully', '2024-11-28 17:17:17.000000', '2024-11-28 17:17:14.000000', 3);
INSERT INTO `logs` VALUES (45, '2024-11-28', b'0', 1, 'ETL process begins', '2024-11-28 17:18:10.000000', '2024-11-28 17:18:10.000000', 3);
INSERT INTO `logs` VALUES (46, '2024-11-28', b'0', 1, 'Getting all data source config to crawl.', '2024-11-28 17:18:10.000000', '2024-11-28 17:18:10.000000', 1);
INSERT INTO `logs` VALUES (47, '2024-11-28', b'0', 1, 'DataSource: DataSourceConfig(id=1, name=CellphoneS, crawlConfigURL=crawl_conf/cellphones.json, crawlSaveLocation=crawl_conf/cellphones.csv, stagingTableName=staging_head_phone, dailyStagingTableName=staging_head_phone_daily, dailyWarehouseTableName=warehouse_records, stagingFields=null, warehouseFields=null, deleted=false)', '2024-11-28 17:18:10.000000', '2024-11-28 17:18:10.000000', 1);
INSERT INTO `logs` VALUES (48, '2024-11-28', b'0', 1, 'Success.', '2024-11-28 17:18:12.000000', '2024-11-28 17:18:10.000000', 1);
INSERT INTO `logs` VALUES (49, '2024-11-28', b'0', 1, 'Starting data loading to warehouse', '2024-11-28 17:18:12.000000', '2024-11-28 17:18:12.000000', 2);
INSERT INTO `logs` VALUES (50, '2024-11-28', b'0', 3, 'Error loading data to warehouse: StatementCallback; bad SQL grammar [CALL load_data_to_warehouse()]', '2024-11-28 17:18:12.000000', '2024-11-28 17:18:12.000000', 2);
INSERT INTO `logs` VALUES (51, '2024-11-28', b'0', 1, 'ETL process ended successfully', '2024-11-28 17:18:12.000000', '2024-11-28 17:18:10.000000', 3);
INSERT INTO `logs` VALUES (52, '2024-11-28', b'0', 1, 'ETL process begins', '2024-11-28 17:18:53.000000', '2024-11-28 17:18:53.000000', 3);
INSERT INTO `logs` VALUES (53, '2024-11-28', b'0', 1, 'Getting all data source config to crawl.', '2024-11-28 17:18:53.000000', '2024-11-28 17:18:53.000000', 1);
INSERT INTO `logs` VALUES (54, '2024-11-28', b'0', 1, 'DataSource: DataSourceConfig(id=1, name=CellphoneS, crawlConfigURL=crawl_conf/cellphones.json, crawlSaveLocation=crawl_conf/cellphones.csv, stagingTableName=staging_head_phone, dailyStagingTableName=staging_head_phone_daily, dailyWarehouseTableName=warehouse_records, stagingFields=null, warehouseFields=null, deleted=false)', '2024-11-28 17:18:53.000000', '2024-11-28 17:18:53.000000', 1);
INSERT INTO `logs` VALUES (55, '2024-11-28', b'0', 1, 'Success.', '2024-11-28 17:18:54.000000', '2024-11-28 17:18:53.000000', 1);
INSERT INTO `logs` VALUES (56, '2024-11-28', b'0', 1, 'Starting data loading to warehouse', '2024-11-28 17:18:54.000000', '2024-11-28 17:18:54.000000', 2);
INSERT INTO `logs` VALUES (57, '2024-11-28', b'0', 3, 'Error loading data to warehouse: StatementCallback; bad SQL grammar [CALL load_data_to_warehouse()]', '2024-11-28 17:18:54.000000', '2024-11-28 17:18:54.000000', 2);
INSERT INTO `logs` VALUES (58, '2024-11-28', b'0', 1, 'ETL process ended successfully', '2024-11-28 17:18:54.000000', '2024-11-28 17:18:53.000000', 3);
INSERT INTO `logs` VALUES (59, '2024-11-28', b'0', 1, 'ETL process begins', '2024-11-28 17:22:08.000000', '2024-11-28 17:22:08.000000', 3);
INSERT INTO `logs` VALUES (60, '2024-11-28', b'0', 1, 'Getting all data source config to crawl.', '2024-11-28 17:22:08.000000', '2024-11-28 17:22:08.000000', 1);
INSERT INTO `logs` VALUES (61, '2024-11-28', b'0', 1, 'DataSource: DataSourceConfig(id=1, name=CellphoneS, crawlConfigURL=crawl_conf/cellphones.json, crawlSaveLocation=crawl_conf/cellphones.csv, stagingTableName=staging_head_phone, dailyStagingTableName=staging_head_phone_daily, dailyWarehouseTableName=warehouse_records, stagingFields=null, warehouseFields=null, deleted=false)', '2024-11-28 17:22:08.000000', '2024-11-28 17:22:08.000000', 1);
INSERT INTO `logs` VALUES (62, '2024-11-28', b'0', 1, 'Success.', '2024-11-28 17:22:10.000000', '2024-11-28 17:22:08.000000', 1);
INSERT INTO `logs` VALUES (63, '2024-11-28', b'0', 1, 'Starting data loading to warehouse', '2024-11-28 17:22:10.000000', '2024-11-28 17:22:10.000000', 2);
INSERT INTO `logs` VALUES (64, '2024-11-28', b'0', 3, 'Error loading data to warehouse: StatementCallback; bad SQL grammar [CALL load_data_to_warehouse()]', '2024-11-28 17:22:10.000000', '2024-11-28 17:22:10.000000', 2);
INSERT INTO `logs` VALUES (65, '2024-11-28', b'0', 1, 'ETL process ended successfully', '2024-11-28 17:22:10.000000', '2024-11-28 17:22:08.000000', 3);
INSERT INTO `logs` VALUES (66, '2024-11-28', b'0', 1, 'ETL process begins', '2024-11-28 17:22:31.000000', '2024-11-28 17:22:31.000000', 3);
INSERT INTO `logs` VALUES (67, '2024-11-28', b'0', 1, 'Getting all data source config to crawl.', '2024-11-28 17:22:31.000000', '2024-11-28 17:22:31.000000', 1);
INSERT INTO `logs` VALUES (68, '2024-11-28', b'0', 1, 'DataSource: DataSourceConfig(id=1, name=CellphoneS, crawlConfigURL=crawl_conf/cellphones.json, crawlSaveLocation=crawl_conf/cellphones.csv, stagingTableName=staging_head_phone, dailyStagingTableName=staging_head_phone_daily, dailyWarehouseTableName=warehouse_records, stagingFields=null, warehouseFields=null, deleted=false)', '2024-11-28 17:22:31.000000', '2024-11-28 17:22:31.000000', 1);
INSERT INTO `logs` VALUES (69, '2024-11-28', b'0', 1, 'Success.', '2024-11-28 17:22:33.000000', '2024-11-28 17:22:31.000000', 1);
INSERT INTO `logs` VALUES (70, '2024-11-28', b'0', 1, 'Starting data loading to warehouse', '2024-11-28 17:22:33.000000', '2024-11-28 17:22:33.000000', 2);
INSERT INTO `logs` VALUES (71, '2024-11-28', b'0', 3, 'Error loading data to warehouse: StatementCallback; bad SQL grammar [CALL load_data_to_warehouse()]', '2024-11-28 17:22:33.000000', '2024-11-28 17:22:33.000000', 2);
INSERT INTO `logs` VALUES (72, '2024-11-28', b'0', 1, 'ETL process ended successfully', '2024-11-28 17:22:33.000000', '2024-11-28 17:22:31.000000', 3);
INSERT INTO `logs` VALUES (73, '2024-11-28', b'0', 1, 'ETL process begins', '2024-11-28 17:26:33.000000', '2024-11-28 17:26:33.000000', 3);
INSERT INTO `logs` VALUES (74, '2024-11-28', b'0', 1, 'Getting all data source config to crawl.', '2024-11-28 17:26:34.000000', '2024-11-28 17:26:34.000000', 1);
INSERT INTO `logs` VALUES (75, '2024-11-28', b'0', 1, 'DataSource: DataSourceConfig(id=1, name=CellphoneS, crawlConfigURL=crawl_conf/cellphones.json, crawlSaveLocation=crawl_conf/cellphones.csv, stagingTableName=staging_head_phone, dailyStagingTableName=staging_head_phone_daily, dailyWarehouseTableName=warehouse_records, stagingFields=null, warehouseFields=null, deleted=false)', '2024-11-28 17:26:34.000000', '2024-11-28 17:26:34.000000', 1);
INSERT INTO `logs` VALUES (76, '2024-11-28', b'0', 1, 'Success.', '2024-11-28 17:26:35.000000', '2024-11-28 17:26:34.000000', 1);
INSERT INTO `logs` VALUES (77, '2024-11-28', b'0', 1, 'Starting data loading to warehouse', '2024-11-28 17:26:36.000000', '2024-11-28 17:26:36.000000', 2);
INSERT INTO `logs` VALUES (78, '2024-11-28', b'0', 3, 'Error loading data to warehouse: StatementCallback; bad SQL grammar [CALL load_data_to_warehouse()]', '2024-11-28 17:26:36.000000', '2024-11-28 17:26:36.000000', 2);
INSERT INTO `logs` VALUES (79, '2024-11-28', b'0', 1, 'ETL process ended successfully', '2024-11-28 17:26:36.000000', '2024-11-28 17:26:33.000000', 3);
INSERT INTO `logs` VALUES (80, '2024-11-28', b'0', 1, 'ETL process begins', '2024-11-28 17:27:09.000000', '2024-11-28 17:27:09.000000', 3);
INSERT INTO `logs` VALUES (81, '2024-11-28', b'0', 1, 'Getting all data source config to crawl.', '2024-11-28 17:27:09.000000', '2024-11-28 17:27:09.000000', 1);
INSERT INTO `logs` VALUES (82, '2024-11-28', b'0', 1, 'DataSource: DataSourceConfig(id=1, name=CellphoneS, crawlConfigURL=crawl_conf/cellphones.json, crawlSaveLocation=crawl_conf/cellphones.csv, stagingTableName=staging_head_phone, dailyStagingTableName=staging_head_phone_daily, dailyWarehouseTableName=warehouse_records, stagingFields=null, warehouseFields=null, deleted=false)', '2024-11-28 17:27:09.000000', '2024-11-28 17:27:09.000000', 1);
INSERT INTO `logs` VALUES (83, '2024-11-28', b'0', 1, 'Success.', '2024-11-28 17:27:10.000000', '2024-11-28 17:27:09.000000', 1);
INSERT INTO `logs` VALUES (84, '2024-11-28', b'0', 1, 'Starting data loading to warehouse', '2024-11-28 17:27:10.000000', '2024-11-28 17:27:10.000000', 2);
INSERT INTO `logs` VALUES (85, '2024-11-28', b'0', 3, 'Error loading data to warehouse: StatementCallback; bad SQL grammar [CALL load_data_to_warehouse()]', '2024-11-28 17:27:10.000000', '2024-11-28 17:27:10.000000', 2);
INSERT INTO `logs` VALUES (86, '2024-11-28', b'0', 1, 'ETL process ended successfully', '2024-11-28 17:27:10.000000', '2024-11-28 17:27:09.000000', 3);

-- ----------------------------
-- Table structure for process_tracker
-- ----------------------------
DROP TABLE IF EXISTS `process_tracker`;
CREATE TABLE `process_tracker`  (
  `process_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `deleted` bit(1) NOT NULL,
  `process_last_end_time` datetime(6) NULL DEFAULT NULL,
  `process_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `process_last_start_time` datetime(6) NULL DEFAULT NULL,
  `process_status` enum('C_E','C_FE','C_RE','C_SE','P_FR','P_R','P_RR','P_SR','S_FI','S_I','S_RI','S_SI','W_FI','W_I','W_RI','W_SI') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`process_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of process_tracker
-- ----------------------------
INSERT INTO `process_tracker` VALUES (1, b'0', '2024-11-28 17:27:10.000000', 'com.nicha.etl.service.CrawlService', '2024-11-28 17:27:09.000000', 'C_SE');
INSERT INTO `process_tracker` VALUES (2, b'0', NULL, 'Load Data', NULL, 'W_RI');
INSERT INTO `process_tracker` VALUES (3, b'0', '2024-11-28 17:27:10.000000', 'com.nicha.etl.service.ETLService', '2024-11-28 17:27:09.000000', 'P_SR');

SET FOREIGN_KEY_CHECKS = 1;
