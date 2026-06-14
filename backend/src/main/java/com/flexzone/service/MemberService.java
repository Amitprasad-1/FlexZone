package com.flexzone.service;

import com.flexzone.dto.MemberProfileDTO;
import com.flexzone.entity.BmiLog;
import com.flexzone.entity.MemberProfile;
import com.flexzone.repository.BmiLogRepository;
import com.flexzone.repository.MemberProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class MemberService {

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    @Autowired
    private BmiLogRepository bmiLogRepository;

    public MemberProfileDTO getProfile(Long id) {
        MemberProfile profile = memberProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member profile not found"));
        return convertToMemberDTO(profile);
    }

    public List<BmiLog> getBmiHistory(Long memberId) {
        MemberProfile profile = memberProfileRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member profile not found"));
        return bmiLogRepository.findByMemberOrderByIdDesc(profile);
    }

    @Transactional
    public BmiLog logBmi(Long memberId, BigDecimal heightCm, BigDecimal weightKg) {
        MemberProfile profile = memberProfileRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member profile not found"));

        // calculate BMI: weight (kg) / height (m)^2
        BigDecimal heightM = heightCm.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal heightSq = heightM.multiply(heightM);
        BigDecimal bmi = weightKg.divide(heightSq, 2, RoundingMode.HALF_UP);

        BmiLog bmiLog = BmiLog.builder()
                .member(profile)
                .heightCm(heightCm)
                .weightKg(weightKg)
                .calculatedBmi(bmi)
                .loggedDate(LocalDate.now())
                .build();

        return bmiLogRepository.save(bmiLog);
    }

    private MemberProfileDTO convertToMemberDTO(MemberProfile profile) {
        return MemberProfileDTO.builder()
                .id(profile.getId())
                .username(profile.getUser().getUsername())
                .email(profile.getUser().getEmail())
                .fullName(profile.getUser().getFullName())
                .membershipStatus(profile.getMembershipStatus())
                .membershipPlanId(profile.getMembershipPlan() != null ? profile.getMembershipPlan().getId() : null)
                .membershipPlanName(profile.getMembershipPlan() != null ? profile.getMembershipPlan().getName() : "None")
                .membershipStartDate(profile.getMembershipStartDate())
                .membershipEndDate(profile.getMembershipEndDate())
                .assignedTrainerId(profile.getAssignedTrainer() != null ? profile.getAssignedTrainer().getId() : null)
                .assignedTrainerName(profile.getAssignedTrainer() != null ? profile.getAssignedTrainer().getUser().getFullName() : "Unassigned")
                .profilePicture(profile.getUser().getProfilePicture())
                .build();
    }
}
