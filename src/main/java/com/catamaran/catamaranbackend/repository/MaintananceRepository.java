package com.catamaran.catamaranbackend.repository;

import com.catamaran.catamaranbackend.domain.MaintananceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintananceRepository extends JpaRepository<MaintananceEntity, Long> {
    Page<MaintananceEntity> findByBoatId(Long boatId, Pageable pageable);
}

