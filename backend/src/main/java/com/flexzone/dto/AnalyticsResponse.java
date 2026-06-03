package com.flexzone.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnalyticsResponse {
    private long totalActiveMembers;
    private long totalTrainers;
    private BigDecimal totalRevenue;
    private Map<String, BigDecimal> monthlyRevenue; // Month Name -> Revenue
    private Map<String, Double> slotOccupancyRates; // Slot Time -> Percentage occupied
    private List<MemberProfileDTO> expiringMemberships;
    private List<PaymentDTO> recentPayments;

    @Data
    @Builder
    public static class PaymentDTO {
        private Long id;
        private String memberName;
        private BigDecimal amount;
        private String paymentType;
        private String paymentMethod;
        private String transactionId;
        private String status;
        private String paymentDate;
    }
}
