package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;

import com.tartis_recon_ai_parking.domain.tariff.Tariff;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TariffPersistenceMapper {


    TariffEntity toEntity(Tariff tariff);

    Tariff toDomain(TariffEntity entity);

    @ObjectFactory
    default Tariff create(TariffEntity entity) {
        if (entity == null) {
            return null;
        }
        return Tariff.reconstruct(
                entity.getUniqueId(),
                entity.getName(),
                entity.getType(),
                entity.getPricePerMinute(),
                entity.getBasePrice(),
                entity.isActive(),
                entity.getVersion()
        );
    }

}