package com.flexzone.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PaymentRequest {
    private BigDecimal amount;
    private String paymentType; // 'MEMBERSHIP_RENEWAL', 'PRODUCT_PURCHASE'
    private String paymentMethod; // 'RAZORPAY', 'PAYTM'
    private Long planId; // populated if paymentType is 'MEMBERSHIP_RENEWAL'
    
    // For shop purchases:
    private List<CartItemDTO> cartItems;

    @Data
    public static class CartItemDTO {
        private Long productId;
        private Integer quantity;
    }
}
