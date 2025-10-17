package com.catamaran.catamaranbackend.repository;

import com.catamaran.catamaranbackend.domain.MaintananceEntity;
import com.catamaran.catamaranbackend.domain.MaintananceStatus;
import com.catamaran.catamaranbackend.domain.MaintananceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintananceRepository extends JpaRepository<MaintananceEntity, Long>, JpaSpecificationExecutor<MaintananceEntity> {
    Page<MaintananceEntity> findByBoatId(Long boatId, Pageable pageable);

    long countByStatus(MaintananceStatus status);

    @Query("SELECT SUM(m.cost) FROM MaintananceEntity m WHERE m.cost IS NOT NULL")
    Double sumTotalCost();

    // Additional query methods for specific filtering
    Page<MaintananceEntity> findByStatus(MaintananceStatus status, Pageable pageable);

    Page<MaintananceEntity> findByType(MaintananceType type, Pageable pageable);

    Page<MaintananceEntity> findByStatusAndType(MaintananceStatus status, MaintananceType type, Pageable pageable);
}

