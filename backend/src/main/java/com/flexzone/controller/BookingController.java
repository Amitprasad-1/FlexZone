package com.flexzone.controller;

import com.flexzone.dto.BookingDTO;
import com.flexzone.entity.ClassSchedule;
import com.flexzone.entity.TimeSlot;
import com.flexzone.entity.User;
import com.flexzone.repository.UserRepository;
import com.flexzone.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    private Long getAuthenticatedUserId(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        return user.getId();
    }

    @GetMapping("/slots")
    public ResponseEntity<List<TimeSlot>> getAllTimeSlots() {
        return ResponseEntity.ok(bookingService.getAllTimeSlots());
    }

    @GetMapping("/classes")
    public ResponseEntity<List<ClassSchedule>> getAllClassSchedules() {
        return ResponseEntity.ok(bookingService.getAllClassSchedules());
    }

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getMyBookings(Principal principal) {
        Long memberId = getAuthenticatedUserId(principal);
        return ResponseEntity.ok(bookingService.getMemberBookings(memberId));
    }

    @PostMapping
    public ResponseEntity<?> createBooking(Principal principal, @RequestBody Map<String, Object> request) {
        try {
            Long memberId = getAuthenticatedUserId(principal);
            String type = (String) request.get("bookingType");
            Long targetId = Long.valueOf(request.get("targetId").toString());
            
            LocalDate date = LocalDate.now();
            if (request.get("bookingDate") != null) {
                date = LocalDate.parse((String) request.get("bookingDate"));
            }

            BookingDTO booking = bookingService.createBooking(memberId, type, targetId, date);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(Principal principal, @PathVariable Long id) {
        try {
            Long memberId = getAuthenticatedUserId(principal);
            bookingService.cancelBooking(id, memberId);
            return ResponseEntity.ok("Booking cancelled successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
