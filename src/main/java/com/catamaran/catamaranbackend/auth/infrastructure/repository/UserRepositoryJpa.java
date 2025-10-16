package com.catamaran.catamaranbackend.auth.infrastructure.repository;

import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
import com.catamaran.catamaranbackend.domain.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepositoryJpa extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByResetToken(String resetToken);
    Page<UserEntity> findAllByRole(Role role, Pageable pageable);
    UserEntity getUserEntityById(Long id);

    List<UserEntity> findByRole(Role role);
}
