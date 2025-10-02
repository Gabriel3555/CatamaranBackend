package com.catamaran.catamaranbackend.repository;

import com.catamaran.catamaranbackend.domain.MaintananceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaintananceRepository extends JpaRepository<MaintananceEntity, Long> {
    List<MaintananceEntity> findByBoatId(Long boatId);
}

