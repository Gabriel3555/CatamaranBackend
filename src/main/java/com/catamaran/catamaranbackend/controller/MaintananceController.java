package com.catamaran.catamaranbackend.controller;

import com.catamaran.catamaranbackend.domain.MaintananceEntity;
import com.catamaran.catamaranbackend.repository.BoatRepository;
import com.catamaran.catamaranbackend.repository.MaintananceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenances")
@RequiredArgsConstructor
public class MaintananceController {

    private final MaintananceRepository maintananceRepository;
    private final BoatRepository boatRepository;

    @PostMapping("/{boatId}")
    public ResponseEntity<MaintananceEntity> createMaintenance(
            @PathVariable Long boatId,
            @RequestBody MaintananceEntity request
    ) {
        return boatRepository.findById(boatId)
                .map(boat -> {
                    request.setBoat(boat);
                    MaintananceEntity saved = maintananceRepository.save(request);

                    // Aumentar deuda del barco
                    if (request.getCost() != null) {
                        boat.setBalance(boat.getBalance() + request.getCost());
                        boatRepository.save(boat);
                    }

                    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/{boatId}")
    public ResponseEntity<List<MaintananceEntity>> getByBoat(@PathVariable Long boatId) {
        return ResponseEntity.ok(maintananceRepository.findByBoatId(boatId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaintananceEntity> updateMaintenance(
            @PathVariable Long id,
            @RequestBody MaintananceEntity updated
    ) {
        return maintananceRepository.findById(id)
                .map(existing -> {
                    existing.setStatus(updated.getStatus());
                    existing.setPriority(updated.getPriority());
                    existing.setDatePerformed(updated.getDatePerformed());
                    existing.setDescription(updated.getDescription());
                    return ResponseEntity.ok(maintananceRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaintenance(@PathVariable Long id) {
        if (maintananceRepository.existsById(id)) {
            maintananceRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
