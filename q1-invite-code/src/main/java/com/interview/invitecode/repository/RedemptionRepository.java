package com.interview.invitecode.repository;

import com.interview.invitecode.entity.Redemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RedemptionRepository extends JpaRepository<Redemption, Long> {

    List<Redemption> findByCodeId(Long codeId);

    boolean existsByCodeIdAndUserId(Long codeId, String userId);
}
