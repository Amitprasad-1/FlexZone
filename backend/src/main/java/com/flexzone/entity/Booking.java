package com.flexzone.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"member_id", "time_slot_id", "booking_date"}),
    @UniqueConstraint(columnNames = {"member_id", "class_schedule_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberProfile member;

    @Column(name = "booking_type", nullable = false, length = 20)
    private String bookingType; // 'SLOT' or 'CLASS'

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "time_slot_id")
    private TimeSlot timeSlot;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_schedule_id")
    private ClassSchedule classSchedule;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
