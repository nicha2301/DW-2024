package com.nicha.etl.entity.defaults;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "staging_head_phone")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StagingHeadPhone {

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

    @Column(name = "created_at", insertable = false, updatable = false)
    private String createdAt;

}


