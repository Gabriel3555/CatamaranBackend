package com.catamaran.catamaranbackend.repository;

import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
import com.catamaran.catamaranbackend.domain.BoatEntity;
import com.catamaran.catamaranbackend.domain.BoatType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoatRepository extends JpaRepository<BoatEntity, Long> {
    List<BoatEntity> findByOwner(UserEntity owner);
    Page<BoatEntity> findByOwner(UserEntity owner, Pageable pageable);

    // Método corregido para búsqueda y filtrado
    @Query("SELECT b FROM BoatEntity b WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.location) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:type IS NULL OR b.type = :type) AND " +
            "(:status = 'all' OR " +
            "(:status = 'Disponible' AND b.owner IS NULL) OR " +
            "(:status = 'Ocupado' AND b.owner IS NOT NULL))")
    Page<BoatEntity> findWithFilters(@Param("search") String search,
                                     @Param("type") BoatType type,
                                     @Param("status") String status,
                                     Pageable pageable);

    // Método alternativo para búsqueda más eficiente - solo búsqueda de texto
    @Query("SELECT b FROM BoatEntity b WHERE " +
            "(LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(b.location) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<BoatEntity> findWithSearchFilters(@Param("search") String search,
                                           Pageable pageable);

    // Método para filtrar por tipo
    @Query("SELECT b FROM BoatEntity b WHERE " +
            "(:type IS NULL OR b.type = :type)")
    Page<BoatEntity> findByType(@Param("type") BoatType type,
                                Pageable pageable);

    // Método para filtrar por estado
    @Query("SELECT b FROM BoatEntity b WHERE " +
            "(:status = 'all' OR " +
            "(:status = 'Disponible' AND b.owner IS NULL) OR " +
            "(:status = 'Ocupado' AND b.owner IS NOT NULL))")
    Page<BoatEntity> findByStatus(@Param("status") String status,
                                  Pageable pageable);
}