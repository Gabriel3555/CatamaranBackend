package com.catamaran.catamaranbackend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "maintenances")
public class MaintananceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "boat_id")
    private BoatEntity boat;

    @Enumerated(EnumType.STRING)
    private MaintananceType type;

    @Enumerated(EnumType.STRING)
    private MaintananceStatus status;

    @Enumerated(EnumType.STRING)
    private MaintenancePriority priority;

    private LocalDateTime dateScheduled;
    private LocalDateTime datePerformed;
    private String description;

    private Double cost;
}
