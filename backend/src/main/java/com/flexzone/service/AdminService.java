package com.flexzone.service;

import com.flexzone.dto.AnalyticsResponse;
import com.flexzone.dto.MemberProfileDTO;
import com.flexzone.dto.TrainerDTO;
import com.flexzone.entity.*;
import com.flexzone.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainerProfileRepository trainerProfileRepository;

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    @Autowired
    private MembershipPlanRepository membershipPlanRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    // Trainers CRUD
    public List<TrainerDTO> getAllTrainers() {
        return trainerProfileRepository.findAll().stream()
                .map(this::convertToTrainerDTO)
                .sorted((t1, t2) -> t2.getId().compareTo(t1.getId()))
                .collect(Collectors.toList());
    }

    public TrainerDTO getTrainerById(Long id) {
        TrainerProfile profile = trainerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trainer profile not found"));
        return convertToTrainerDTO(profile);
    }

    @Transactional
    public TrainerDTO updateTrainer(Long id, TrainerDTO dto) {
        TrainerProfile profile = trainerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trainer profile not found"));
        
        User user = profile.getUser();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        userRepository.save(user);

        profile.setSpecialization(dto.getSpecialization());
        profile.setBio(dto.getBio());
        profile.setExperienceYears(dto.getExperienceYears());
        
        TrainerProfile saved = trainerProfileRepository.save(profile);
        return convertToTrainerDTO(saved);
    }

    @Transactional
    public void deleteTrainer(Long id) {
        trainerProfileRepository.deleteById(id);
        userRepository.deleteById(id);
    }

    // Members CRUD
    public List<MemberProfileDTO> getAllMembers() {
        return memberProfileRepository.findAll().stream()
                .map(this::convertToMemberDTO)
                .sorted((m1, m2) -> m2.getId().compareTo(m1.getId()))
                .collect(Collectors.toList());
    }

    public MemberProfileDTO getMemberById(Long id) {
        MemberProfile profile = memberProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member profile not found"));
        return convertToMemberDTO(profile);
    }

    @Transactional
    public MemberProfileDTO updateMember(Long id, MemberProfileDTO dto) {
        MemberProfile profile = memberProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member profile not found"));

        User user = profile.getUser();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        userRepository.save(user);

        profile.setMembershipStatus(dto.getMembershipStatus());
        profile.setMembershipStartDate(dto.getMembershipStartDate());
        profile.setMembershipEndDate(dto.getMembershipEndDate());

        if (dto.getMembershipPlanId() != null) {
            MembershipPlan plan = membershipPlanRepository.findById(dto.getMembershipPlanId())
                    .orElseThrow(() -> new RuntimeException("Plan not found"));
            profile.setMembershipPlan(plan);
        } else {
            profile.setMembershipPlan(null);
        }

        if (dto.getAssignedTrainerId() != null) {
            TrainerProfile trainer = trainerProfileRepository.findById(dto.getAssignedTrainerId())
                    .orElseThrow(() -> new RuntimeException("Trainer not found"));
            profile.setAssignedTrainer(trainer);
        } else {
            profile.setAssignedTrainer(null);
        }

        MemberProfile saved = memberProfileRepository.save(profile);
        return convertToMemberDTO(saved);
    }

    @Transactional
    public void deleteMember(Long id) {
        memberProfileRepository.deleteById(id);
        userRepository.deleteById(id);
    }

    // Membership Plans CRUD
    public List<MembershipPlan> getAllPlans() {
        return membershipPlanRepository.findAll();
    }

    public MembershipPlan createPlan(MembershipPlan plan) {
        return membershipPlanRepository.save(plan);
    }

    public MembershipPlan updatePlan(Long id, MembershipPlan planDetails) {
        MembershipPlan plan = membershipPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        plan.setName(planDetails.getName());
        plan.setDescription(planDetails.getDescription());
        plan.setPrice(planDetails.getPrice());
        plan.setDurationDays(planDetails.getDurationDays());
        return membershipPlanRepository.save(plan);
    }

    public void deletePlan(Long id) {
        membershipPlanRepository.deleteById(id);
    }

    // Analytics Dashboard
    public AnalyticsResponse getDashboardAnalytics() {
        long activeMembers = memberProfileRepository.findByMembershipStatus("ACTIVE").size();
        long totalTrainers = trainerProfileRepository.count();
        BigDecimal rawRevenue = paymentRepository.sumTotalRevenue();
        BigDecimal totalRevenue = rawRevenue != null ? rawRevenue : BigDecimal.ZERO;

        // Expiring Soon (in next 7 days)
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        List<MemberProfileDTO> expiringMembers = memberProfileRepository.findExpiringSoonMemberships(today, nextWeek).stream()
                .map(this::convertToMemberDTO)
                .collect(Collectors.toList());

        // Recent Payments
        List<Payment> recentPaymentsList = paymentRepository.findAll();
        recentPaymentsList.sort((p1, p2) -> p2.getPaymentDate().compareTo(p1.getPaymentDate()));
        List<AnalyticsResponse.PaymentDTO> recentPayments = recentPaymentsList.stream()
                .limit(10)
                .map(p -> AnalyticsResponse.PaymentDTO.builder()
                        .id(p.getId())
                        .memberName(p.getMember().getUser().getFullName())
                        .amount(p.getAmount())
                        .paymentType(p.getPaymentType())
                        .paymentMethod(p.getPaymentMethod())
                        .transactionId(p.getTransactionId())
                        .status(p.getStatus())
                        .paymentDate(p.getPaymentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                        .build())
                .collect(Collectors.toList());

        // Monthly Revenue (Aggregate successful payments of current year)
        Map<String, BigDecimal> monthlyRevenue = new LinkedHashMap<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (String m : months) {
            monthlyRevenue.put(m, BigDecimal.ZERO);
        }

        int currentYear = LocalDate.now().getYear();
        List<Payment> allSuccessPayments = paymentRepository.findAll().stream()
                .filter(p -> "SUCCESS".equals(p.getStatus()) && p.getPaymentDate().getYear() == currentYear)
                .collect(Collectors.toList());

        for (Payment p : allSuccessPayments) {
            int monthIdx = p.getPaymentDate().getMonthValue() - 1;
            String monthName = months[monthIdx];
            monthlyRevenue.put(monthName, monthlyRevenue.get(monthName).add(p.getAmount()));
        }

        // Slot Occupancy rates
        Map<String, Double> occupancyRates = new LinkedHashMap<>();
        List<TimeSlot> slots = timeSlotRepository.findAll();
        for (TimeSlot slot : slots) {
            long bookingsCount = bookingRepository.countByTimeSlotAndBookingDate(slot, today);
            double occupancy = slot.getMaxCapacity() > 0 ? ((double) bookingsCount / slot.getMaxCapacity()) * 100.0 : 0.0;
            String slotLabel = slot.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) + "-" + slot.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            occupancyRates.put(slotLabel, Double.valueOf(Math.round(occupancy * 10.0) / 10.0));
        }

        return AnalyticsResponse.builder()
                .totalActiveMembers(activeMembers)
                .totalTrainers(totalTrainers)
                .totalRevenue(totalRevenue)
                .expiringMemberships(expiringMembers)
                .recentPayments(recentPayments)
                .monthlyRevenue(monthlyRevenue)
                .slotOccupancyRates(occupancyRates)
                .build();
    }

    private TrainerDTO convertToTrainerDTO(TrainerProfile profile) {
        return TrainerDTO.builder()
                .id(profile.getId())
                .username(profile.getUser().getUsername())
                .email(profile.getUser().getEmail())
                .fullName(profile.getUser().getFullName())
                .specialization(profile.getSpecialization())
                .bio(profile.getBio())
                .experienceYears(profile.getExperienceYears())
                .build();
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
                .build();
    }
}
