package com.ot.mapper;

import com.ot.dto.anesthesiaDrug.AnesthesiaDrugResponse;
import com.ot.entity.AnesthesiaDrug;

public class AnesthesiaDrugMapper {
	
    // Mapper
    public static AnesthesiaDrugResponse mapToResponse(AnesthesiaDrug drug) {
        return AnesthesiaDrugResponse.builder()
                .id(drug.getId())
                .intraOpId(drug.getIntraOp().getId())
                .drugName(drug.getDrugName())
                .dose(drug.getDose())
                .doseUnit(drug.getDoseUnit())
                .route(drug.getRoute())
                .drugType(drug.getDrugType())
                .administeredAt(drug.getAdministeredAt())
                .administeredBy(drug.getAdministeredBy())
                .notes(drug.getNotes())
                .createdAt(drug.getCreatedAt())
                .build();
    }
}
