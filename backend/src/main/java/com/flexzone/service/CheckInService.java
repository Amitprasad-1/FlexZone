package com.flexzone.service;

import com.flexzone.dto.CheckInDTO;
import com.flexzone.entity.Attendance;
import com.flexzone.entity.MemberProfile;
import com.flexzone.entity.User;
import com.flexzone.repository.AttendanceRepository;
import com.flexzone.repository.MemberProfileRepository;
import com.flexzone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CheckInService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public CheckInDTO checkInMember(String scanInput, String adminUsername) {
        MemberProfile member = null;

        // Try parsing as ID
        try {
            Long memberId = Long.parseLong(scanInput.trim());
            member = memberProfileRepository.findById(memberId).orElse(null);
        } catch (NumberFormatException e) {
            // Not a numeric ID, fallback to username/email
        }

        if (member == null) {
            // Try searching by username
            User user = userRepository.findByUsername(scanInput.trim()).orElse(null);
            if (user != null) {
                member = memberProfileRepository.findById(user.getId()).orElse(null);
            }
        }

        if (member == null) {
            // Try searching by email
            User user = userRepository.findByEmail(scanInput.trim()).orElse(null);
            if (user != null) {
                member = memberProfileRepository.findById(user.getId()).orElse(null);
            }
        }

        if (member == null) {
            throw new RuntimeException("Member not found with ID, username, or email: " + scanInput);
        }

        if (!"ACTIVE".equals(member.getMembershipStatus())) {
            throw new RuntimeException("Cannot check in. Membership status is " + member.getMembershipStatus());
        }

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Logged in admin/staff user not found"));

        Attendance attendance = Attendance.builder()
                .member(member)
                .checkInTime(LocalDateTime.now())
                .verifiedByAdmin(admin)
                .build();

        Attendance saved = attendanceRepository.save(attendance);

        return CheckInDTO.builder()
                .memberId(saved.getMember().getId())
                .memberName(saved.getMember().getUser().getFullName())
                .memberUsername(saved.getMember().getUser().getUsername())
                .membershipPlanName(saved.getMember().getMembershipPlan() != null ? saved.getMember().getMembershipPlan().getName() : "No Plan")
                .checkInTime(saved.getCheckInTime())
                .verifiedByAdminName(admin.getFullName())
                .profilePicture(saved.getMember().getUser().getProfilePicture())
                .build();
    }

    public List<CheckInDTO> getTodayCheckIns() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        return attendanceRepository.findByCheckInTimeBetweenOrderByCheckInTimeDesc(startOfDay, endOfDay).stream()
                .map(a -> CheckInDTO.builder()
                        .memberId(a.getMember().getId())
                        .memberName(a.getMember().getUser().getFullName())
                        .memberUsername(a.getMember().getUser().getUsername())
                        .membershipPlanName(a.getMember().getMembershipPlan() != null ? a.getMember().getMembershipPlan().getName() : "No Plan")
                        .checkInTime(a.getCheckInTime())
                        .verifiedByAdminName(a.getVerifiedByAdmin().getFullName())
                        .profilePicture(a.getMember().getUser().getProfilePicture())
                        .build())
                .collect(Collectors.toList());
    }
}
