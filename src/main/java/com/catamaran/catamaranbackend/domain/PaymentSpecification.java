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

    /**
     * Specification for search term filter
     * Searches in owner name, email, or invoice_url
     */
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

            // Search in boat name using LEFT JOIN with FETCH to avoid lazy loading issues
            try {
                jakarta.persistence.criteria.Join<PaymentEntity, BoatEntity> boatJoin =
                    root.join("boat", jakarta.persistence.criteria.JoinType.LEFT);

                // Join owner for search purposes (don't fetch since we don't need it in results)

                // Search in boat name (only if boat exists)
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(
                        criteriaBuilder.coalesce(boatJoin.get("name"), "")
                    ),
                    searchPattern
                ));

                // Search in boat owner information using join
                jakarta.persistence.criteria.Join<BoatEntity, UserEntity> ownerJoin =
                    boatJoin.join("owner", jakarta.persistence.criteria.JoinType.LEFT);

                // Search in owner full name (only if owner exists)
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(
                        criteriaBuilder.coalesce(ownerJoin.get("fullName"), "")
                    ),
                    searchPattern
                ));

                // Search in owner email (only if owner exists)
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(
                        criteriaBuilder.coalesce(ownerJoin.get("email"), "")
                    ),
                    searchPattern
                ));

            } catch (Exception e) {
                // If joins fail, continue with other searches
                // This can happen if the entities have lazy loading issues
                System.out.println("Warning: Could not create joins for search: " + e.getMessage());
                e.printStackTrace();
                // Don't rethrow the exception - just log it and continue with available searches
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