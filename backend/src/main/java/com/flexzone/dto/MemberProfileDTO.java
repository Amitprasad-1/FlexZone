package com.flexzone.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class MemberProfileDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String membershipStatus;
    private Long membershipPlanId;
    private String membershipPlanName;
    private LocalDate membershipStartDate;
    private LocalDate membershipEndDate;
    private Long assignedTrainerId;
    private String assignedTrainerName;
    private String profilePicture;
    private String paymentStatus;
}
