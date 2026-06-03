package com.flexzone.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberProfile member;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "payment_type", nullable = false, length = 50)
    private String paymentType; // 'MEMBERSHIP_RENEWAL', 'PRODUCT_PURCHASE'

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod; // 'RAZORPAY', 'PAYTM', 'CASH'

    @Column(name = "transaction_id", nullable = false, unique = true, length = 100)
    private String transactionId;

    @Column(nullable = false, length = 20)
    private String status; // 'SUCCESS', 'FAILED'
}
