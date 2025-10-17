package com.catamaran.catamaranbackend.domain;

import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentSpecification {

    /**
     * Helper method to check if a join is already being fetched
     */
    private static boolean isJoinAlreadyFetched(jakarta.persistence.criteria.CriteriaQuery<?> query, String attributeName) {
        try {
            return query.getRoots().stream()
                    .flatMap(root -> root.getFetches().stream())
                    .anyMatch(fetch -> fetch.getAttribute().getName().equals(attributeName));
        } catch (Exception e) {
            return false;
        }
    }

    public static Specification<PaymentEntity> hasSearchTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";

            List<Predicate> predicates = new ArrayList<>();

            // Search in invoice_url (always available)
            predicates.add(criteriaBuilder.like(
                criteriaBuilder.lower(
                    criteriaBuilder.coalesce(root.get("invoice_url"), "")
                ),
                searchPattern
            ));

            // Search in payment ID (convert to string)
            predicates.add(criteriaBuilder.like(
                criteriaBuilder.lower(
                    criteriaBuilder.coalesce(
                        criteriaBuilder.concat("", criteriaBuilder.toString(root.get("id"))),
                        ""
                    )
                ),
                "%" + searchTerm.toLowerCase() + "%"
            ));

            try {
                jakarta.persistence.criteria.Join<PaymentEntity, BoatEntity> boatJoin =
                    root.join("boat", jakarta.persistence.criteria.JoinType.LEFT);

                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(
                        criteriaBuilder.coalesce(boatJoin.get("name"), "")
                    ),
                    searchPattern
                ));

                jakarta.persistence.criteria.Join<BoatEntity, UserEntity> ownerJoin =
                    boatJoin.join("owner", jakarta.persistence.criteria.JoinType.LEFT);

                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(
                        criteriaBuilder.coalesce(ownerJoin.get("fullName"), "")
                    ),
                    searchPattern
                ));

                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(
                        criteriaBuilder.coalesce(ownerJoin.get("email"), "")
                    ),
                    searchPattern
                ));

            } catch (Exception e) {
                System.out.println("Warning: Could not create joins for search: " + e.getMessage());
                e.printStackTrace();
            }

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification for reason filter
     */
    public static Specification<PaymentEntity> hasReason(ReasonPayment reason) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("reason"), reason);
    }

    /**
     * Specification for status filter
     */
    public static Specification<PaymentEntity> hasStatus(PaymentStatus status) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("status"), status);
    }

    /**
     * Specification for date range filter
     */
    public static Specification<PaymentEntity> isBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.between(root.get("date"), startDate, endDate);
    }
}