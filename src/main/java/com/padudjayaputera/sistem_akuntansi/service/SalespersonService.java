package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;

import com.padudjayaputera.sistem_akuntansi.dto.SalespersonRequest;
import com.padudjayaputera.sistem_akuntansi.model.Salesperson;

public interface SalespersonService {
    Salesperson createSalesperson(SalespersonRequest request);
    List<Salesperson> getAllSalespeople();
    List<Salesperson> getSalespeopleByDivision(Integer divisionId);
    List<Salesperson> getActiveSalespeopleByDivision(Integer divisionId);
    Salesperson getSalespersonById(Integer id);
    Salesperson updateSalesperson(Integer id, SalespersonRequest request);
    void deleteSalesperson(Integer id);
}
