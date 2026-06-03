package com.flexzone.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingDTO {
    private Long id;
    private Long memberId;
    private String memberName;
    private String bookingType; // 'SLOT' or 'CLASS'
    private Long timeSlotId;
    private String timeSlotText;
    private Long classScheduleId;
    private String classScheduleText;
    private LocalDate bookingDate;
    private LocalDateTime createdAt;
}
