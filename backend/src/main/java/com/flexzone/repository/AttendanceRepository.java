package com.flexzone.repository;

import com.flexzone.entity.Attendance;
import com.flexzone.entity.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByMember(MemberProfile member);
    List<Attendance> findByCheckInTimeBetweenOrderByCheckInTimeDesc(LocalDateTime start, LocalDateTime end);
    long countByCheckInTimeBetween(LocalDateTime start, LocalDateTime end);
}
