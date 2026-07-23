package com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest;

import com.tartis_recon_ai_parking.application.tariff.dto.TariffDTO;
import com.tartis_recon_ai_parking.application.tariff.usecase.ActivateTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.CreateTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.DeactivateTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.GetActiveTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.GetAllTariffsUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.GetTariffUseCase;
import com.tartis_recon_ai_parking.application.tariff.usecase.UpdateTariffUseCase;
import com.tartis_recon_ai_parking.domain.tariff.VehicleType;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request.PriceRequest;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request.TariffCreateRequest;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request.TariffStatusRequest;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.request.TariffUpdateRequest;
import com.tartis_recon_ai_parking.infrastructure.tariff.adapter.input.rest.dto.response.TariffResponse;

import io.micrometer.core.ipc.http.HttpSender.Response;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/tariffs")
public class TariffRestAdapter {

    private final CreateTariffUseCase createTariffUseCase;
    private final GetTariffUseCase getTariffUseCase;
    private final GetAllTariffsUseCase getAllTariffsUseCase;
    private final GetActiveTariffUseCase getActiveTariffUseCase;
    private final UpdateTariffUseCase updateTariffUseCase;
    private final ActivateTariffUseCase activateTariffUseCase;
    private final DeactivateTariffUseCase deactivateTariffUseCase;
    private final TariffRestMapper mapper;

    public TariffRestAdapter(CreateTariffUseCase createTariffUseCase,
                              GetTariffUseCase getTariffUseCase,
                              GetAllTariffsUseCase getAllTariffsUseCase,
                              GetActiveTariffUseCase getActiveTariffUseCase,
                              UpdateTariffUseCase updateTariffUseCase,
                              ActivateTariffUseCase activateTariffUseCase,
                              DeactivateTariffUseCase deactivateTariffUseCase,
                              TariffRestMapper mapper) {
        this.createTariffUseCase = createTariffUseCase;
        this.getTariffUseCase = getTariffUseCase;
        this.getAllTariffsUseCase = getAllTariffsUseCase;
        this.getActiveTariffUseCase = getActiveTariffUseCase;
        this.updateTariffUseCase = updateTariffUseCase;
        this.activateTariffUseCase = activateTariffUseCase;
        this.deactivateTariffUseCase = deactivateTariffUseCase;
        this.mapper = mapper;
    }

    // GET /v1/tariffs/active?type={vehicleType}
    // Declarado antes que "/{id}" para que Spring no intente resolver
    // "active" como UUID.
    @GetMapping("/active")
    public ResponseEntity<List<TariffResponse>> getActive(@RequestParam VehicleType type) {
        List<TariffDTO> dtos = getActiveTariffUseCase.execute(type);
        return ResponseEntity.ok(mapper.toResponseList(dtos));
    }

    // GET /v1/tariffs
    @GetMapping
    public ResponseEntity<List<TariffResponse>> getAll() {
        List<TariffDTO> dtos = getAllTariffsUseCase.execute();
        return ResponseEntity.ok(mapper.toResponseList(dtos));
    }

    // GET /v1/tariffs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TariffResponse> getById(@PathVariable UUID id) {
        TariffDTO dto = getTariffUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(dto));
    }

    // POST /v1/tariffs
    @PostMapping
    public ResponseEntity<TariffResponse> create(@Valid @RequestBody TariffCreateRequest request) {
        TariffDTO dto = createTariffUseCase.execute(mapper.toCreateDTO(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(dto));
    }

    // PUT /v1/tariffs/{id}
    @PutMapping("/{id}")
    public ResponseEntity<TariffResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody TariffUpdateRequest request) {
        TariffDTO dto = updateTariffUseCase.execute(id, mapper.toUpdateDTO(request));
        return ResponseEntity.ok(mapper.toResponse(dto));
    }

    // PATCH /v1/tariffs/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<TariffResponse> changeStatus(@PathVariable UUID id,
                                                         @Valid @RequestBody TariffStatusRequest request) {
        TariffDTO dto = Boolean.TRUE.equals(request.getActive())
                ? activateTariffUseCase.execute(id)
                : deactivateTariffUseCase.execute(id);
        return ResponseEntity.ok(mapper.toResponse(dto));
    }

    @PostMapping("/calculate")
    public ResponseEntity<TariffResponse> calculatePrice(@RequestBody PriceRequest request) {
        
        //PriceTransferDTO = priceCalculator.execute(request.getVehicleType(), request.getMinutes());

        
        return null;
    }
    
}