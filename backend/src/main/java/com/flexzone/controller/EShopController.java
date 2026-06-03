package com.flexzone.controller;

import com.flexzone.dto.PaymentRequest;
import com.flexzone.entity.MembershipPlan;
import com.flexzone.entity.Order;
import com.flexzone.entity.Payment;
import com.flexzone.entity.Product;
import com.flexzone.entity.User;
import com.flexzone.repository.MembershipPlanRepository;
import com.flexzone.repository.UserRepository;
import com.flexzone.service.EShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/shop")
public class EShopController {

    @Autowired
    private EShopService eShopService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembershipPlanRepository membershipPlanRepository;

    private Long getAuthenticatedUserId(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        return user.getId();
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(eShopService.getAllProducts());
    }

    @GetMapping("/plans")
    public ResponseEntity<List<MembershipPlan>> getPlans() {
        return ResponseEntity.ok(membershipPlanRepository.findAll());
    }

    @PostMapping("/payment")
    public ResponseEntity<?> makePayment(Principal principal, @RequestBody PaymentRequest request) {
        try {
            Long memberId = getAuthenticatedUserId(principal);
            Payment payment = eShopService.processPayment(memberId, request);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getMyOrders(Principal principal) {
        Long memberId = getAuthenticatedUserId(principal);
        return ResponseEntity.ok(eShopService.getMemberOrders(memberId));
    }
}
