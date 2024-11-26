package com.nicha.etl.entity.config;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@Entity
@Table(schema = "control_db", name = "logs")
public class ProcessLogging {

    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    @Id
    @Column(name = "log_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_process_id")
    private ProcessTracker processTracker;

    @Column(name = "log_process_start")
    private Timestamp processStart;

    @Column(name = "log_process_end")
    private Timestamp processEnd;

    @Column(name = "log_level")
    @Enumerated(EnumType.ORDINAL)
    private LogLevel level;

    @Column(name = "log_message")
    private String message;

    @Column(name = "log_date")
    private Date date;
}
