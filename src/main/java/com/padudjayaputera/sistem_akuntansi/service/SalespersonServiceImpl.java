package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.padudjayaputera.sistem_akuntansi.dto.SalespersonRequest;
import com.padudjayaputera.sistem_akuntansi.model.Division;
import com.padudjayaputera.sistem_akuntansi.model.Salesperson;
import com.padudjayaputera.sistem_akuntansi.repository.DivisionRepository;
import com.padudjayaputera.sistem_akuntansi.repository.SalespersonRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SalespersonServiceImpl implements SalespersonService {

    private final SalespersonRepository salespersonRepository;
    private final DivisionRepository divisionRepository;

    @Override
    @Transactional
    public Salesperson createSalesperson(SalespersonRequest request) {
        // ✅ ADD: Log untuk debug divisionId yang diterima
        System.out.println("🔍 CREATE SALESPERSON - Received request:");
        System.out.println("  - Nama: " + request.getNama());
        System.out.println("  - DivisionId: " + request.getDivisionId());
        System.out.println("  - Status: " + request.getStatus());
        
        Division division = divisionRepository.findById(request.getDivisionId())
            .orElseThrow(() -> new RuntimeException("Division dengan ID " + request.getDivisionId() + " tidak ditemukan"));

        System.out.println("✅ CREATE SALESPERSON - Found division: " + division.getName());

        Salesperson salesperson = new Salesperson();
        salesperson.setNama(request.getNama());
        salesperson.setStatus(request.getStatus());
        salesperson.setDivision(division);
        
        try {
            Salesperson saved = salespersonRepository.save(salesperson);
            System.out.println("✅ CREATE SALESPERSON - Saved with ID: " + saved.getId());
            
            // ✅ ADD: Flush untuk memastikan data tersimpan ke database
            salespersonRepository.flush();
            System.out.println("✅ CREATE SALESPERSON - Flushed to database");
            
            return saved;
        } catch (Exception e) {
            System.err.println("❌ CREATE SALESPERSON - Error saving: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<Salesperson> getAllSalespeople() {
        return salespersonRepository.findAll();
    }

    @Override
    public List<Salesperson> getSalespeopleByDivision(Integer divisionId) {
        return salespersonRepository.findByDivisionId(divisionId);
    }

    @Override
    public List<Salesperson> getActiveSalespeopleByDivision(Integer divisionId) {
        return salespersonRepository.findByDivisionIdAndStatus(divisionId, "AKTIF");
    }

    @Override
    public Salesperson getSalespersonById(Integer id) {
        return salespersonRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Salesperson dengan ID " + id + " tidak ditemukan"));
    }

    @Override
    public Salesperson updateSalesperson(Integer id, SalespersonRequest request) {
        Salesperson salesperson = getSalespersonById(id);
        
        Division division = divisionRepository.findById(request.getDivisionId())
            .orElseThrow(() -> new RuntimeException("Division dengan ID " + request.getDivisionId() + " tidak ditemukan"));

        salesperson.setNama(request.getNama());
        salesperson.setStatus(request.getStatus());
        salesperson.setDivision(division);
        
        return salespersonRepository.save(salesperson);
    }

    @Override
    public void deleteSalesperson(Integer id) {
        Salesperson salesperson = getSalespersonById(id);
        salespersonRepository.delete(salesperson);
    }
}
