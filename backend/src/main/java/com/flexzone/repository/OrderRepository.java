package com.flexzone.repository;

import com.flexzone.entity.MemberProfile;
import com.flexzone.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByMemberOrderByIdDesc(MemberProfile member);
}
