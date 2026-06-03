package com.flexzone.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CheckInDTO {
    private Long memberId;
    private String memberName;
    private LocalDateTime checkInTime;
    private String verifiedByAdminName;
}
