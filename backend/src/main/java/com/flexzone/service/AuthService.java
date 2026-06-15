package com.flexzone.service;

import com.flexzone.config.JwtTokenProvider;
import com.flexzone.dto.JwtResponse;
import com.flexzone.dto.LoginRequest;
import com.flexzone.dto.SignupRequest;
import com.flexzone.entity.*;
import com.flexzone.repository.MemberProfileRepository;
import com.flexzone.repository.MembershipPlanRepository;
import com.flexzone.repository.TrainerProfileRepository;
import com.flexzone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AuthService {


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainerProfileRepository trainerProfileRepository;

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    @Autowired
    private MembershipPlanRepository membershipPlanRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String jwt = tokenProvider.generateToken(authentication, user.getRole().name());

        return new JwtResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), user.getRole().name(), user.getProfilePicture());
    }

    @Transactional
    public User registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Email Address already in use!");
        }

        Role role = Role.valueOf(signupRequest.getRole().toUpperCase());

        User user = User.builder()
                .username(signupRequest.getUsername())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .email(signupRequest.getEmail())
                .fullName(signupRequest.getFullName())
                .role(role)
                .profilePicture(signupRequest.getProfilePicture())
                .build();

        User savedUser = userRepository.save(user);

        if (role == Role.TRAINER) {
            TrainerProfile trainerProfile = TrainerProfile.builder()
                    .user(savedUser)
                    .specialization(signupRequest.getSpecialization() != null ? signupRequest.getSpecialization() : "General Fitness")
                    .bio(signupRequest.getBio() != null ? signupRequest.getBio() : "")
                    .experienceYears(signupRequest.getExperienceYears() != null ? signupRequest.getExperienceYears() : 0)
                    .build();
            trainerProfileRepository.save(trainerProfile);
        } else if (role == Role.MEMBER) {
            MemberProfile memberProfile = MemberProfile.builder()
                    .user(savedUser)
                    .membershipStatus("PENDING")
                    .build();

            if (signupRequest.getMembershipPlanId() != null) {
                MembershipPlan plan = membershipPlanRepository.findById(signupRequest.getMembershipPlanId())
                        .orElseThrow(() -> new RuntimeException("Membership Plan not found"));
                memberProfile.setMembershipPlan(plan);
                memberProfile.setMembershipStatus("PENDING");
            }

            memberProfileRepository.save(memberProfile);
        }

        return savedUser;
    }

    public void verifyEmail(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email address not found!");
        }
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public List<MembershipPlan> getAllPlans() {
        return membershipPlanRepository.findAll();
    }
}
