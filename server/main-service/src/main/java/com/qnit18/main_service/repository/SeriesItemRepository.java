package com.qnit18.main_service.repository;

import com.qnit18.main_service.entity.SeriesItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeriesItemRepository extends JpaRepository<SeriesItem, UUID> {
    List<SeriesItem> findByChartDataId(UUID chartDataId);
}

