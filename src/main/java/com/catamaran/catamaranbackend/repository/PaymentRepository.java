package com.catamaran.catamaranbackend.repository;

import com.catamaran.catamaranbackend.domain.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Page<PaymentEntity> findByBoatId(Long boatId, Pageable pageable);
}
