package com.nicha.etl.entity.config;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "logs")
@SQLDelete(sql = "UPDATE config_db.logs SET deleted = 1 WHERE log_id = ?")
@SQLRestriction("deleted = 0")
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

    @Column(name = "log_message", length = Integer.MAX_VALUE)
    private String message;

    @Column(name = "log_date")
    private Date date;

    private boolean deleted;
}
