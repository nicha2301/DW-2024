package com.nicha.etl.entity.defaults;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "head_phone")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private String productId;

    private String name;
    private String brand;
    private String type;
    private BigDecimal price;

    @Column(name = "warranty_info")
    private String warrantyInfo;

    private String feature;

    @Column(name = "voice_control")
    private String voiceControl;

    private String microphone;

    @Column(name = "battery_life")
    private String batteryLife;

    private String dimensions;
    private String weight;
    private String compatibility;

    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    private boolean isDelete;

    @Column(name = "date_delete")
    private Date dateDelete;

    @Column(name = "date_insert")
    private Timestamp dateInsert;

    @Column(name = "expired_date")
    private Date expiredDate;

}
