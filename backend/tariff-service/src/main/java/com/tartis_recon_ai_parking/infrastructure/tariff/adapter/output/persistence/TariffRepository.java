package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; 
import java.util.UUID;
import java.util.List;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;


public interface TariffRepository extends JpaRepository<TariffEntity, UUID> {

    // Método para comprobar existencia
    boolean existsByName(String name);

    // Método para buscar por Nombre
    Optional<TariffEntity> findByName(String name);

    // Método para buscar por estado activo/inactivo
    List<TariffEntity> findByActive(boolean active);

    // Método para buscar activas por tipo
    List<TariffEntity> findByActiveTrueAndType(VehicleType type);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT t FROM TariffEntity t WHERE t.active = true AND t.type = :type")
    List<TariffEntity> findByActiveTrueAndTypeForUpdate(@org.springframework.data.repository.query.Param("type") VehicleType type);

    /**
     * Apaga de golpe las tarifas activas del tipo (IN-17).
     *
     * <p>Es un update masivo y no un bucle de save() a proposito: los save()
     * solo encolan en la ActionQueue de Hibernate y se vuelcan juntos en el
     * commit, sin garantizar que el apagado se escriba antes que el encendido.
     * Como ux_tariffs_one_active_per_type es un indice unico parcial y no
     * diferible, ese instante con dos filas activas aborta la transaccion.
     * El JPQL masivo se ejecuta ya, antes de activar la nueva.
     *
     * <p>version se incrementa a mano: un update masivo no pasa por @Version,
     * y sin esto las entidades que otra sesion tenga cargadas seguirian
     * creyendose al dia.
     */
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query(
            "UPDATE TariffEntity t SET t.active = false, t.version = t.version + 1 "
                    + "WHERE t.type = :type AND t.active = true")
    int deactivateAllActiveByType(@org.springframework.data.repository.query.Param("type") VehicleType type);

    /** Igual que el anterior, pero respeta la tarifa que se esta activando. */
    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @org.springframework.data.jpa.repository.Query(
            "UPDATE TariffEntity t SET t.active = false, t.version = t.version + 1 "
                    + "WHERE t.type = :type AND t.active = true AND t.uniqueId <> :excludeId")
    int deactivateOtherActiveByType(@org.springframework.data.repository.query.Param("type") VehicleType type,
                                    @org.springframework.data.repository.query.Param("excludeId") UUID excludeId);

}
