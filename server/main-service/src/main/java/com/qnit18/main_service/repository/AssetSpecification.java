package com.qnit18.main_service.repository;

import com.qnit18.main_service.constant.AssetCategory;
import com.qnit18.main_service.entity.Asset;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AssetSpecification {

    public static Specification<Asset> searchAssets(String q, AssetCategory category) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by q parameter (symbol or assetName)
            if (q != null && !q.trim().isEmpty()) {
                String searchPattern = "%" + q.trim().toLowerCase() + "%";
                Predicate symbolPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("symbol")), searchPattern
                );
                Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("assetName")), searchPattern
                );
                predicates.add(criteriaBuilder.or(symbolPredicate, namePredicate));
            }

            // Filter by category (skip if null)
            if (category != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

