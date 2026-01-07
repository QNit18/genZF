package com.qnit18.main_service.util;

import org.springframework.data.domain.Sort;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AssetUtil {

    public static Sort parseSort(String sortParam) { // Helper method to parse sort parameter
        if (sortParam == null || sortParam.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "changePercentage");
        }

        String[] parts = sortParam.split(",");
        if (parts.length != 2) {
            return Sort.by(Sort.Direction.DESC, "changePercentage");
        }

        String field = parts[0].trim();
        String direction = parts[1].trim().toLowerCase();
        
        Sort.Direction sortDirection = direction.equals("asc") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        return Sort.by(sortDirection, field);
    }
}
