package com.catamaran.catamaranbackend.domain;

import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "boats")
public class BoatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private BoatType type;

    private String name;
    private String model;
    private String location;
    private Double price;

    @OneToMany(mappedBy = "boat", cascade = CascadeType.ALL)
    private List<MaintananceEntity> maintanances;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "document_id")
    private List<BoatDocumentEntity> documents;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private UserEntity owner;

    @OneToMany(mappedBy = "boat", cascade = CascadeType.ALL)
    private List<PaymentEntity> payments;

    @Column(nullable = false)
    private Double balance = 0.0;
}
