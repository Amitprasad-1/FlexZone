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
    public ResponseEntity<?> scanCheckIn(Principal principal, @RequestBody Map<String, Object> request) {
        try {
            Object inputObj = request.get("memberId");
            if (inputObj == null) {
                inputObj = request.get("scanInput");
            }
            if (inputObj == null || String.valueOf(inputObj).trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Scan input is required.");
            }
            String scanInput = String.valueOf(inputObj);
            CheckInDTO dto = checkInService.checkInMember(scanInput, principal.getName());
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
