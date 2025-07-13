package com.padudjayaputera.sistem_akuntansi.dto;

import com.padudjayaputera.sistem_akuntansi.model.Salesperson;

import lombok.Getter;

@Getter
public class SalespersonResponse {
    
    private final Integer id;
    private final String nama;
    private final String status;
    private final String namaDivision;
    private final Integer divisionId;

    public SalespersonResponse(Salesperson salesperson) {
        this.id = salesperson.getId();
        this.nama = salesperson.getNama();
        this.status = salesperson.getStatus();
        this.namaDivision = salesperson.getDivision() != null ? salesperson.getDivision().getName() : null;
        this.divisionId = salesperson.getDivision() != null ? salesperson.getDivision().getId() : null;
    }
}
