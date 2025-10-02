package com.catamaran.catamaranbackend.repository;

import com.catamaran.catamaranbackend.domain.BoatDocumentEntity;
import com.catamaran.catamaranbackend.domain.BoatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoatDocumentRepository extends JpaRepository<BoatDocumentEntity, Long> {
    Page<BoatDocumentEntity> findAll(Pageable pageable);
}
