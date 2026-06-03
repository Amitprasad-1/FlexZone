package com.flexzone.service;

import com.flexzone.dto.BookingDTO;
import com.flexzone.entity.*;
import com.flexzone.repository.BookingRepository;
import com.flexzone.repository.ClassScheduleRepository;
import com.flexzone.repository.MemberProfileRepository;
import com.flexzone.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private ClassScheduleRepository classScheduleRepository;

    public List<TimeSlot> getAllTimeSlots() {
        return timeSlotRepository.findAll();
    }

    public List<ClassSchedule> getAllClassSchedules() {
        return classScheduleRepository.findByStartTimeAfter(LocalDateTime.now().minusHours(2));
    }

    public List<BookingDTO> getMemberBookings(Long memberId) {
        MemberProfile member = memberProfileRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return bookingRepository.findByMember(member).stream()
                .map(this::convertToDTO)
                .sorted((b1, b2) -> b2.getId().compareTo(b1.getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingDTO createBooking(Long memberId, String type, Long targetId, LocalDate date) {
        MemberProfile member = memberProfileRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        if (!"ACTIVE".equals(member.getMembershipStatus())) {
            throw new RuntimeException("Cannot make a booking. Your membership is currently " + member.getMembershipStatus());
        }

        Booking.BookingBuilder bookingBuilder = Booking.builder()
                .member(member)
                .bookingType(type)
                .bookingDate(date);

        if ("SLOT".equalsIgnoreCase(type)) {
            TimeSlot slot = timeSlotRepository.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("Time Slot not found"));
            
            // Check double booking for slots on same day
            if (bookingRepository.existsByMemberAndTimeSlotAndBookingDate(member, slot, date)) {
                throw new RuntimeException("You have already booked this time slot for " + date);
            }

            // Check capacity
            long currentBookings = bookingRepository.countByTimeSlotAndBookingDate(slot, date);
            if (currentBookings >= slot.getMaxCapacity()) {
                throw new RuntimeException("This slot is fully booked for " + date);
            }

            bookingBuilder.timeSlot(slot);

        } else if ("CLASS".equalsIgnoreCase(type)) {
            ClassSchedule schedule = classScheduleRepository.findById(targetId)
                    .orElseThrow(() -> new RuntimeException("Class Schedule not found"));

            // Check double booking for class
            if (bookingRepository.existsByMemberAndClassSchedule(member, schedule)) {
                throw new RuntimeException("You are already registered for this class!");
            }

            // Check capacity
            long currentBookings = bookingRepository.countByClassSchedule(schedule);
            if (currentBookings >= schedule.getMaxCapacity()) {
                throw new RuntimeException("This class is fully booked!");
            }

            bookingBuilder.classSchedule(schedule);
            bookingBuilder.bookingDate(schedule.getStartTime().toLocalDate());
        } else {
            throw new RuntimeException("Invalid booking type. Must be SLOT or CLASS.");
        }

        Booking saved = bookingRepository.save(bookingBuilder.build());
        return convertToDTO(saved);
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long memberId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getMember().getId().equals(memberId)) {
            throw new RuntimeException("Unauthorized cancellation request.");
        }
        bookingRepository.delete(booking);
    }

    private BookingDTO convertToDTO(Booking booking) {
        String slotText = "";
        if (booking.getTimeSlot() != null) {
            slotText = booking.getTimeSlot().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                       booking.getTimeSlot().getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        }

        String classText = "";
        if (booking.getClassSchedule() != null) {
            classText = booking.getClassSchedule().getClassName() + " (" +
                        booking.getClassSchedule().getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + ")";
        }

        return BookingDTO.builder()
                .id(booking.getId())
                .memberId(booking.getMember().getId())
                .memberName(booking.getMember().getUser().getFullName())
                .bookingType(booking.getBookingType())
                .timeSlotId(booking.getTimeSlot() != null ? booking.getTimeSlot().getId() : null)
                .timeSlotText(slotText)
                .classScheduleId(booking.getClassSchedule() != null ? booking.getClassSchedule().getId() : null)
                .classScheduleText(classText)
                .bookingDate(booking.getBookingDate())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
