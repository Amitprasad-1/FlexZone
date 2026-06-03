package com.flexzone.repository;

import com.flexzone.entity.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {
    List<MemberProfile> findByMembershipStatus(String status);

    @Query("SELECT mp FROM MemberProfile mp WHERE mp.membershipEndDate < :date AND mp.membershipStatus = 'ACTIVE'")
    List<MemberProfile> findExpiredMemberships(@Param("date") LocalDate date);

    @Query("SELECT mp FROM MemberProfile mp WHERE mp.membershipEndDate >= :today AND mp.membershipEndDate <= :threshold AND mp.membershipStatus = 'ACTIVE'")
    List<MemberProfile> findExpiringSoonMemberships(@Param("today") LocalDate today, @Param("threshold") LocalDate threshold);
}
