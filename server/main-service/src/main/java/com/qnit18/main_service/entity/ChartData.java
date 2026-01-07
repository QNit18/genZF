package com.qnit18.main_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qnit18.main_service.constant.Timeframe;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "chart_data")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChartData {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Timeframe timeframe;

    @JsonIgnore
    @OneToMany(mappedBy = "chartData", cascade = CascadeType.ALL, orphanRemoval = true)
    List<SeriesItem> seriesItems;
}

