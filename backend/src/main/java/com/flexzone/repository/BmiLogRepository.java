package com.flexzone.repository;

import com.flexzone.entity.BmiLog;
import com.flexzone.entity.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BmiLogRepository extends JpaRepository<BmiLog, Long> {
    List<BmiLog> findByMemberOrderByIdDesc(MemberProfile member);
}
