package com.catamaran.catamaranbackend.repository;

import com.catamaran.catamaranbackend.domain.PaymentEntity;
import com.catamaran.catamaranbackend.domain.PaymentStatus;
import com.catamaran.catamaranbackend.domain.ReasonPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long>, JpaSpecificationExecutor<PaymentEntity> {
    Page<PaymentEntity> findByBoatId(Long boatId, Pageable pageable);

    Page<PaymentEntity> findByReason(ReasonPayment reason, Pageable pageable);

    Page<PaymentEntity> findByStatus(PaymentStatus status, Pageable pageable);

    Page<PaymentEntity> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT p FROM PaymentEntity p WHERE " +
           "LOWER(p.boat.owner.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.boat.owner.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.invoice_url) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<PaymentEntity> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);
}
