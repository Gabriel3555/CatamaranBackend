package com.catamaran.catamaranbackend.controller;

import com.catamaran.catamaranbackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.catamaran.catamaranbackend.domain.BoatDocumentEntity;
import com.catamaran.catamaranbackend.domain.BoatEntity;
import com.catamaran.catamaranbackend.repository.BoatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boat")
public class BoatController {

    private final BoatRepository boatRepository;
    private final UserRepositoryJpa userRepository;

    @PostMapping
    public ResponseEntity<BoatEntity> createBoat(@RequestBody BoatEntity boat) {
        boat.setBalance(boat.getPrice() != null ? boat.getPrice() : 0.0);

        BoatEntity saved = boatRepository.save(boat);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<Double> getBoatBalance(@PathVariable Long id) {
        return boatRepository.findById(id)
                .map(boat -> ResponseEntity.ok(boat.getBalance()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<BoatEntity>> getAllBoats(Pageable pageable) {
        return ResponseEntity.ok(boatRepository.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoatEntity> getBoatById(@PathVariable Long id) {
        return boatRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BoatEntity> updateBoat(
            @PathVariable Long id,
            @RequestBody BoatEntity updatedBoat) {
        return boatRepository.findById(id)
                .map(existing -> {
                    existing.setName(updatedBoat.getName());
                    existing.setModel(updatedBoat.getModel());
                    existing.setLocation(updatedBoat.getLocation());
                    existing.setPrice(updatedBoat.getPrice());
                    existing.setType(updatedBoat.getType());
                    return ResponseEntity.ok(boatRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoat(@PathVariable Long id) {
        if (boatRepository.existsById(id)) {
            boatRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{boatId}/documents")
    public ResponseEntity<List<BoatDocumentEntity>> getBoatDocuments(@PathVariable Long boatId) {
        return boatRepository.findById(boatId)
                .map(boat -> ResponseEntity.ok(boat.getDocuments()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{userId}")
    public ResponseEntity<List<BoatEntity>> getBoatsByOwner(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(user.getBoats()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{boatId}/documents")
    public ResponseEntity<BoatEntity> addBoatDocument(
            @PathVariable Long boatId,
            @RequestBody BoatDocumentEntity newDoc
    ) {
        return boatRepository.findById(boatId)
                .map(boat -> {
                    List<BoatDocumentEntity> docs = boat.getDocuments();
                    if (docs == null) {
                        docs = new ArrayList<>();
                        boat.setDocuments(docs);
                    }
                    docs.add(newDoc);
                    BoatEntity saved = boatRepository.save(boat);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{boatId}/documents/{index}")
    public ResponseEntity<BoatEntity> deleteBoatDocument(
            @PathVariable Long boatId,
            @PathVariable int index
    ) {
        Optional<BoatEntity> optionalBoat = boatRepository.findById(boatId);

        if (optionalBoat.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BoatEntity boat = optionalBoat.get();
        List<BoatDocumentEntity> docs = boat.getDocuments();

        if (docs == null || index < 0 || index >= docs.size()) {
            return ResponseEntity.badRequest().build();
        }

        docs.remove(index);
        BoatEntity saved = boatRepository.save(boat);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{boatId}/assign-owner/{userId}")
    public ResponseEntity<BoatEntity> assignOwner(
            @PathVariable Long boatId,
            @PathVariable Long userId
    ) {
        return boatRepository.findById(boatId).flatMap(boat ->
                userRepository.findById(userId).map(user -> {
                    if (boat.getOwner() == null) {
                        boat.setOwner(user);
                        // Agregar precio del barco al balance si es la primera asignación
                        boat.setBalance(boat.getBalance() + (boat.getPrice() != null ? boat.getPrice() : 0.0));
                        BoatEntity updated = boatRepository.save(boat);
                        return ResponseEntity.ok(updated);
                    } else {
                        // Ya tiene dueño → devolver 400 vacío tipado
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).<BoatEntity>build();
                    }
                })
        ).orElse(ResponseEntity.notFound().build());
    }
}
