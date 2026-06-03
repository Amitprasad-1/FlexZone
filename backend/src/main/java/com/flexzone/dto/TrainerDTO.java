package com.flexzone.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrainerDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String specialization;
    private String bio;
    private Integer experienceYears;
}
