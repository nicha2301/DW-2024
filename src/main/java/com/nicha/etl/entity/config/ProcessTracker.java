package com.nicha.etl.entity.config;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(schema = "control_db", name = "process_tracker")
public class ProcessTracker {

    public enum ProcessStatus {
        C_RE, C_E, C_SE, C_FE,
        S_RI, S_I, S_SI, S_FI,
        W_RI, W_I, W_SI, W_FI;
    }

    @Id
    @Column(name = "process_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_name")
    private String processName;

    @Column(name = "process_status")
    @Enumerated(EnumType.STRING)
    private ProcessStatus status;

    @Column(name = "process_last_start_time")
    private Timestamp startTime;

    @Column(name = "process_last_end_time")
    private Timestamp endTime;

}
