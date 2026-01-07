package com.qnit18.main_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "series_items")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeriesItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chart_data_id", nullable = false)
    ChartData chartData;

    @Column(nullable = false)
    Long timestamp;

    @Column(nullable = false, precision = 19, scale = 4)
    BigDecimal price;
}

