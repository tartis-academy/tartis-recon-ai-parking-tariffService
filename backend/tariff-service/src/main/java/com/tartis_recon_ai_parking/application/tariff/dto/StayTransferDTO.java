package com.tartis_recon_ai_parking.application.tariff.dto;


import com.tartis_recon_ai_parking.domain.tariff.VehicleType;

public record StayTransferDTO(VehicleType type, int minutes){}
