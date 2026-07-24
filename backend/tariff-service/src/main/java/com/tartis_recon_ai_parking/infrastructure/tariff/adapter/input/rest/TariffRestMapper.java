package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest;

import com.tartis_recon_ai_parking.application.tariff.dto.PriceTransferDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffCreateDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.dto.TariffUpdateDTO;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request.TariffCreateRequest;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request.TariffUpdateRequest;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.response.PriceResponse;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.response.TariffResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TariffRestMapper {

    TariffCreateDTO toCreateDTO(TariffCreateRequest request);

    TariffUpdateDTO toUpdateDTO(TariffUpdateRequest request);

    @Mapping(target = "id", source = "uniqueId")
    TariffResponse toResponse(TariffDTO dto);

    PriceResponse toResponse(PriceTransferDTO dto);

    List<TariffResponse> toResponseList(List<TariffDTO> dtos);
}