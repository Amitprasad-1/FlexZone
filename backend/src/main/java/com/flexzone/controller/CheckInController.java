package com.flexzone.controller;

import com.flexzone.dto.CheckInDTO;
import com.flexzone.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    @PostMapping("/scan")
    public ResponseEntity<?> scanCheckIn(Principal principal, @RequestBody Map<String, Long> request) {
        try {
            Long memberId = request.get("memberId");
            if (memberId == null) {
                return ResponseEntity.badRequest().body("Member ID is required.");
            }
            CheckInDTO dto = checkInService.checkInMember(memberId, principal.getName());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/today")
    public ResponseEntity<List<CheckInDTO>> getTodayCheckIns() {
        return ResponseEntity.ok(checkInService.getTodayCheckIns());
    }
}
