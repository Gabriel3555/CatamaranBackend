package com.catamaran.catamaranbackend.domain;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentSpecification {

    /**
     * Specification for search term filter
     * Searches in owner name, email, or invoice_url
     */
    public static Specification<PaymentEntity> hasSearchTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";

            List<Predicate> predicates = new ArrayList<>();

            // Search in boat owner full name
            if (root.get("boat").get("owner").get("fullName") != null) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("boat").get("owner").get("fullName")),
                    searchPattern
                ));
            }

            // Search in boat owner email
            if (root.get("boat").get("owner").get("email") != null) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("boat").get("owner").get("email")),
                    searchPattern
                ));
            }

            // Search in invoice_url
            if (root.get("invoice_url") != null) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("invoice_url")),
                    searchPattern
                ));
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