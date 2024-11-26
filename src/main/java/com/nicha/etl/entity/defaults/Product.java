package com.nicha.etl.entity.defaults;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_records")
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
    private LocalDateTime createdAt;
}
