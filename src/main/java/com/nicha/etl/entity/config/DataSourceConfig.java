package com.nicha.etl.entity.config;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.*;

@Data
@Entity
@Table(name = "ds_config")
@SQLDelete(sql = "UPDATE config_db.ds_config SET deleted = 1 WHERE ds_id = ?")
@SQLRestriction("deleted = 0")
public class DataSourceConfig {

    @Id
    @Column(name = "ds_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ds_name")
    private String name;

    @Column(name = "ds_crawl_config_url")
    private String crawlConfigURL;

    @Column(name = "ds_crawl_save_location")
    private String crawlSaveLocation;

    @Column(name = "tbl_staging")
    private String stagingTableName;

    @Column(name = "tbl_staging_daily")
    private String dailyStagingTableName;

    @Column(name = "tbl_warehouse_daily")
    private String dailyWarehouseTableName;

    @Column(name = "fields_staging")
    private String stagingFields;

    @Column(name = "fields_warehouse")
    private String warehouseFields;

    private boolean deleted;

}
