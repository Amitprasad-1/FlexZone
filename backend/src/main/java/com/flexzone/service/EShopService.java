package com.flexzone.service;

import com.flexzone.dto.PaymentRequest;
import com.flexzone.entity.*;
import com.flexzone.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EShopService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    @Autowired
    private MembershipPlanRepository membershipPlanRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Transactional
    public Payment processPayment(Long memberId, PaymentRequest request) {
        MemberProfile member = memberProfileRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // 1. Log Payment transaction
        String txId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Payment payment = Payment.builder()
                .member(member)
                .amount(request.getAmount())
                .paymentDate(LocalDateTime.now())
                .paymentType(request.getPaymentType())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(txId)
                .status("SUCCESS") // Auto-success in simulated environment
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // 2. Perform actions depending on type
        if ("MEMBERSHIP_RENEWAL".equalsIgnoreCase(request.getPaymentType())) {
            MembershipPlan plan = membershipPlanRepository.findById(request.getPlanId())
                    .orElseThrow(() -> new RuntimeException("Membership Plan not found"));

            LocalDate currentEnd = member.getMembershipEndDate();
            LocalDate start = (currentEnd != null && currentEnd.isAfter(LocalDate.now())) ? currentEnd : LocalDate.now();
            
            member.setMembershipPlan(plan);
            member.setMembershipStatus("ACTIVE");
            member.setMembershipStartDate(start);
            member.setMembershipEndDate(start.plusDays(plan.getDurationDays()));
            
            memberProfileRepository.save(member);

        } else if ("PRODUCT_PURCHASE".equalsIgnoreCase(request.getPaymentType())) {
            if (request.getCartItems() == null || request.getCartItems().isEmpty()) {
                throw new RuntimeException("Cart is empty");
            }

            Order order = Order.builder()
                    .member(member)
                    .orderDate(LocalDateTime.now())
                    .totalAmount(request.getAmount())
                    .status("PAID")
                    .orderItems(new ArrayList<>())
                    .build();

            for (PaymentRequest.CartItemDTO item : request.getCartItems()) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

                if (product.getStock() < item.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for product: " + product.getName());
                }

                // Deduct stock
                product.setStock(product.getStock() - item.getQuantity());
                productRepository.save(product);

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .quantity(item.getQuantity())
                        .unitPrice(product.getPrice())
                        .build();

                order.getOrderItems().add(orderItem);
            }

            orderRepository.save(order);
        }

        return savedPayment;
    }

    public List<Order> getMemberOrders(Long memberId) {
        MemberProfile member = memberProfileRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return orderRepository.findByMemberOrderByIdDesc(member);
    }
}
