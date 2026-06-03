package com.flexzone.repository;

import com.flexzone.entity.ClassSchedule;
import com.flexzone.entity.TrainerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    List<ClassSchedule> findByStartTimeAfter(LocalDateTime dateTime);
    List<ClassSchedule> findByTrainer(TrainerProfile trainer);
}
