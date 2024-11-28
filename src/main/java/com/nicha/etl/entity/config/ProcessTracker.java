package com.nicha.etl.entity.config;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.sql.Timestamp;
import java.util.Calendar;

@Data
@Entity
@Table(name = "process_tracker")
@SQLDelete(sql = "UPDATE config_db.process_tracker SET deleted = 1 WHERE process_id = ?")
@SQLRestriction("deleted = 0")
public class ProcessTracker {

    public enum ProcessStatus {
        P_RR, P_R, P_SR, P_FR,
        C_RE, C_E, C_SE, C_FE,
        S_RI, S_I, S_SI, S_FI,
        W_RI, W_I, W_SI, W_FI
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

    public boolean lastStartedToday() {
        if (startTime == null) {
            return false;
        }
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(startTime);
        cal2.setTime(new Timestamp(System.currentTimeMillis()));
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

    private boolean deleted;

}
