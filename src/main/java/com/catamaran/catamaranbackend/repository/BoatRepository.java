package com.catamaran.catamaranbackend.repository;

import com.catamaran.catamaranbackend.domain.BoatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoatRepository extends JpaRepository<BoatEntity, Long> {}
