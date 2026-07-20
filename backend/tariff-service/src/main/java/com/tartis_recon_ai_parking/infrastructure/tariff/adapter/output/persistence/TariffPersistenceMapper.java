package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TariffPersistenceMapper {


    TariffEntity toEntity(Tariff tariff);

    Tariff toDomain(TariffEntity entity);

    
}