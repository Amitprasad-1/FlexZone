package com.flexzone.controller;

import com.flexzone.dto.AnalyticsResponse;
import com.flexzone.dto.MemberProfileDTO;
import com.flexzone.dto.TrainerDTO;
import com.flexzone.entity.MembershipPlan;
import com.flexzone.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // Analytics Dashboard
    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        return ResponseEntity.ok(adminService.getDashboardAnalytics());
    }

    // Trainers Management
    @GetMapping("/trainers")
    public ResponseEntity<List<TrainerDTO>> getAllTrainers() {
        return ResponseEntity.ok(adminService.getAllTrainers());
    }

    @GetMapping("/trainers/{id}")
    public ResponseEntity<TrainerDTO> getTrainerById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getTrainerById(id));
    }

    @PutMapping("/trainers/{id}")
    public ResponseEntity<TrainerDTO> updateTrainer(@PathVariable Long id, @RequestBody TrainerDTO dto) {
        return ResponseEntity.ok(adminService.updateTrainer(id, dto));
    }

    @DeleteMapping("/trainers/{id}")
    public ResponseEntity<?> deleteTrainer(@PathVariable Long id) {
        adminService.deleteTrainer(id);
        return ResponseEntity.ok("Trainer deleted successfully.");
    }

    // Members Management
    @GetMapping("/members")
    public ResponseEntity<List<MemberProfileDTO>> getAllMembers() {
        return ResponseEntity.ok(adminService.getAllMembers());
    }

    @GetMapping("/members/{id}")
    public ResponseEntity<MemberProfileDTO> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getMemberById(id));
    }

    @PutMapping("/members/{id}")
    public ResponseEntity<MemberProfileDTO> updateMember(@PathVariable Long id, @RequestBody MemberProfileDTO dto) {
        return ResponseEntity.ok(adminService.updateMember(id, dto));
    }

    @DeleteMapping("/members/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) {
        adminService.deleteMember(id);
        return ResponseEntity.ok("Member deleted successfully.");
    }

    // Membership Plans Management
    @GetMapping("/plans")
    public ResponseEntity<List<MembershipPlan>> getAllPlans() {
        return ResponseEntity.ok(adminService.getAllPlans());
    }

    @PostMapping("/plans")
    public ResponseEntity<MembershipPlan> createPlan(@RequestBody MembershipPlan plan) {
        return ResponseEntity.ok(adminService.createPlan(plan));
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<MembershipPlan> updatePlan(@PathVariable Long id, @RequestBody MembershipPlan plan) {
        return ResponseEntity.ok(adminService.updatePlan(id, plan));
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<?> deletePlan(@PathVariable Long id) {
        adminService.deletePlan(id);
        return ResponseEntity.ok("Plan deleted successfully.");
    }
}
