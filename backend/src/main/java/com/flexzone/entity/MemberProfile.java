package com.flexzone.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "member_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "assignedTrainer"})
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MemberProfile {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "membership_status", nullable = false, length = 20)
    private String membershipStatus; // 'ACTIVE', 'EXPIRED', 'PENDING'

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "membership_plan_id")
    private MembershipPlan membershipPlan;

    @Column(name = "membership_start_date")
    private LocalDate membershipStartDate;

    @Column(name = "membership_end_date")
    private LocalDate membershipEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_trainer_id")
    private TrainerProfile assignedTrainer;
}
