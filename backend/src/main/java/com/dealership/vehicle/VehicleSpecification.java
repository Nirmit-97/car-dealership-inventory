package com.dealership.vehicle;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for dynamically filtering Vehicle queries.
 *
 * Why Specifications instead of JPQL @Query with nulls:
 *   Hibernate 6 sends null parameters as bytea to PostgreSQL, causing
 *   "function lower(bytea) does not exist" errors. Specifications build
 *   the WHERE clause dynamically — null params simply produce no predicate.
 *
 * Usage in VehicleService:
 *   vehicleRepository.findAll(VehicleSpecification.withFilters(...))
 */
public class VehicleSpecification {

    private VehicleSpecification() {} // utility class — no instantiation

    /**
     * Builds a combined Specification from all optional search filters.
     * Only non-null parameters generate predicates — null = "ignore this filter".
     *
     * @param make      optional make filter (case-insensitive partial match)
     * @param model     optional model filter (case-insensitive partial match)
     * @param category  optional category filter (case-insensitive partial match)
     * @param minPrice  optional minimum price (inclusive)
     * @param maxPrice  optional maximum price (inclusive)
     * @return Specification<Vehicle> that can be passed to findAll()
     */
    public static Specification<Vehicle> withFilters(
            String make, String model, String category,
            Double minPrice, Double maxPrice) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only add predicates for non-null, non-empty params
            if (make != null && !make.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("make")),
                        "%" + make.toLowerCase() + "%"
                ));
            }

            if (model != null && !model.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("model")),
                        "%" + model.toLowerCase() + "%"
                ));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("category")),
                        "%" + category.toLowerCase() + "%"
                ));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // If no filters provided, return all vehicles (no WHERE clause)
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
