package com.flexzone.controller;

import com.flexzone.dto.MemberProfileDTO;
import com.flexzone.entity.BmiLog;
import com.flexzone.entity.User;
import com.flexzone.repository.UserRepository;
import com.flexzone.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserRepository userRepository;

    private Long getAuthenticatedUserId(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        return user.getId();
    }

    @GetMapping("/profile")
    public ResponseEntity<MemberProfileDTO> getProfile(Principal principal) {
        Long id = getAuthenticatedUserId(principal);
        return ResponseEntity.ok(memberService.getProfile(id));
    }

    @GetMapping("/bmi/history")
    public ResponseEntity<List<BmiLog>> getBmiHistory(Principal principal) {
        Long id = getAuthenticatedUserId(principal);
        return ResponseEntity.ok(memberService.getBmiHistory(id));
    }

    @PostMapping("/bmi/log")
    public ResponseEntity<?> logBmi(Principal principal, @RequestBody Map<String, BigDecimal> request) {
        try {
            Long id = getAuthenticatedUserId(principal);
            BigDecimal height = request.get("heightCm");
            BigDecimal weight = request.get("weightKg");
            if (height == null || weight == null) {
                return ResponseEntity.badRequest().body("Height (cm) and Weight (kg) are required.");
            }
            BmiLog log = memberService.logBmi(id, height, weight);
            return ResponseEntity.ok(log);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
