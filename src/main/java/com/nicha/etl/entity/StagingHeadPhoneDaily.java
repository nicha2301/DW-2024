package com.nicha.etl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "staging_head_phone_daily")
public class StagingHeadPhoneDaily {

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
}

