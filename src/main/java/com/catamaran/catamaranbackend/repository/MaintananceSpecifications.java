package com.catamaran.catamaranbackend.repository;

import com.catamaran.catamaranbackend.domain.*;
import org.springframework.data.jpa.domain.Specification;

public class MaintananceSpecifications {

    public static Specification<MaintananceEntity> hasSearchTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String searchPattern = "%" + searchTerm.toLowerCase() + "%";

            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("boat").get("name")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("boat").get("model")), searchPattern)
            );
        };
    }

    public static Specification<MaintananceEntity> hasStatus(MaintananceStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<MaintananceEntity> hasType(MaintananceType type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("type"), type);
        };
    }

    public static Specification<MaintananceEntity> hasBoatName(String boatName) {
        return (root, query, criteriaBuilder) -> {
            if (boatName == null || boatName.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String searchPattern = "%" + boatName.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("boat").get("name")), searchPattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("boat").get("model")), searchPattern)
            );
        };
    }

    public static Specification<MaintananceEntity> belongsToOwner(Long ownerId) {
        return (root, query, criteriaBuilder) -> {
            if (ownerId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("boat").get("owner").get("id"), ownerId);
        };
    }
}