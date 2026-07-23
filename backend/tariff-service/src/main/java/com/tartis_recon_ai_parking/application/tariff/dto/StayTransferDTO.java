package com.tartis_recon_ai_parking.application.tariff.dto;

import java.time.Instant;

import com.tartis_recon_ai_parking.domain.tariff.VehicleType;

public record StayTransferDTO(VehicleType type, Instant minutes){}
