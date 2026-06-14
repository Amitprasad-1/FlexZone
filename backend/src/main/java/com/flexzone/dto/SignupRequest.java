package com.flexzone.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotBlank
    private String role; // 'ADMIN', 'TRAINER', 'MEMBER'

    // Trainer specific fields
    private String specialization;
    private String bio;
    private Integer experienceYears;

    // Member specific fields
    private Long membershipPlanId;

    private String profilePicture;
}
