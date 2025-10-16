package com.catamaran.catamaranbackend.repository;

import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
import com.catamaran.catamaranbackend.domain.BoatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoatRepository extends JpaRepository<BoatEntity, Long> {
    List<BoatEntity> findByOwner(UserEntity owner);
    Page<BoatEntity> findByOwner(UserEntity owner, Pageable pageable);
}
