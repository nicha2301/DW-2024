package com.nicha.etl.entity.defaults;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table( name = "etl_logs")
public class EtlLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_name")
    private String processName;

    @Column(name = "log_message")
    private String logMessage;

    @Column(name = "status")
    private String status;

    @Column(name = "timestamp")
    private Timestamp timestamp;
}
