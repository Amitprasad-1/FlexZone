package com.flexzone.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trainer_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TrainerProfile {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(length = 100)
    private String specialization;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "experience_years", nullable = false)
    private Integer experienceYears;
}
