package com.catamaran.catamaranbackend.domain;

import com.catamaran.catamaranbackend.auth.infrastructure.entity.UserEntity;
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
@Table(name = "payments")
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double mount;
    private LocalDateTime date;
    private String invoice_url;

    @Enumerated(EnumType.STRING)
    private ReasonPayment reason;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @OneToOne
    @JoinColumn(name = "maintanance_id")
    private MaintananceEntity maintanance;

    @ManyToOne
    @JoinColumn(name = "boat_id")
    private BoatEntity boat;
}
