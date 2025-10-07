package com.catamaran.catamaranbackend.dto;

import com.catamaran.catamaranbackend.domain.BoatType;

public record CreateBoatRequest (
    BoatType type, String name, String model, String location, Double price
) {}
