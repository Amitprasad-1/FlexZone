package com.flexzone.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberProfile member;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_admin_id", nullable = false)
    private User verifiedByAdmin;
}
