package com.example.becommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Technician working schedule.
 * Maps to 'technician_schedules' table.
 */
@Entity
@Table(name = "technician_schedules", indexes = {
        @Index(name = "idx_tech_schedules_technician_dow", columnList = "technician_id, day_of_week", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianSchedule {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "technician_id", nullable = false)
    private UUID technicianId;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "is_off", nullable = false)
    @Builder.Default
    private Boolean isOff = false;
}
