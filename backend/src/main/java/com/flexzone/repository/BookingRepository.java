package com.flexzone.repository;

import com.flexzone.entity.Booking;
import com.flexzone.entity.ClassSchedule;
import com.flexzone.entity.MemberProfile;
import com.flexzone.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    long countByTimeSlotAndBookingDate(TimeSlot timeSlot, LocalDate bookingDate);
    long countByClassSchedule(ClassSchedule classSchedule);
    boolean existsByMemberAndTimeSlotAndBookingDate(MemberProfile member, TimeSlot timeSlot, LocalDate bookingDate);
    boolean existsByMemberAndClassSchedule(MemberProfile member, ClassSchedule classSchedule);
    List<Booking> findByMember(MemberProfile member);
    List<Booking> findByBookingDate(LocalDate bookingDate);
}
