package com.padudjayaputera.sistem_akuntansi.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.padudjayaputera.sistem_akuntansi.dto.SalespersonRequest;
import com.padudjayaputera.sistem_akuntansi.dto.SalespersonResponse;
import com.padudjayaputera.sistem_akuntansi.model.Salesperson;
import com.padudjayaputera.sistem_akuntansi.service.SalespersonService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/salespeople")
@RequiredArgsConstructor
public class SalespersonController {

    private final SalespersonService salespersonService;
    private static final String HAS_ACCESS_TO_PEMASARAN = "hasAuthority('SUPER_ADMIN') or (hasAuthority('ADMIN_DIVISI') and authentication.principal.division.name == 'DIVISI PEMASARAN & PENJUALAN')";

    @PostMapping
    @PreAuthorize(HAS_ACCESS_TO_PEMASARAN)
    public ResponseEntity<SalespersonResponse> createSalesperson(@Valid @RequestBody SalespersonRequest request) {
        // ✅ ADD: Log untuk debug request yang diterima
        System.out.println("🚀 SALESPERSON CONTROLLER - Received request:");
        System.out.println("  - Nama: " + request.getNama());
        System.out.println("  - DivisionId: " + request.getDivisionId());
        System.out.println("  - Status: " + request.getStatus());
        
        Salesperson salesperson = salespersonService.createSalesperson(request);
        return new ResponseEntity<>(new SalespersonResponse(salesperson), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize(HAS_ACCESS_TO_PEMASARAN)
    public ResponseEntity<List<SalespersonResponse>> getAllSalespeople() {
        List<Salesperson> salespeople = salespersonService.getAllSalespeople();
        List<SalespersonResponse> responses = salespeople.stream()
            .map(SalespersonResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize(HAS_ACCESS_TO_PEMASARAN)
    public ResponseEntity<SalespersonResponse> getSalespersonById(@PathVariable Integer id) {
        Salesperson salesperson = salespersonService.getSalespersonById(id);
        return ResponseEntity.ok(new SalespersonResponse(salesperson));
    }
    
    // Endpoint khusus untuk alur 3 langkah: Step 2 - Pilih Sales berdasarkan Division
    @GetMapping("/by-division/{divisionId}")
    @PreAuthorize(HAS_ACCESS_TO_PEMASARAN)
    public ResponseEntity<List<SalespersonResponse>> getSalespeopleByDivision(@PathVariable Integer divisionId) {
        // ✅ ADD: Log untuk debug CORS issue
        System.out.println("🔍 GET SALESPEOPLE BY DIVISION - Request received:");
        System.out.println("  - DivisionId: " + divisionId);
        System.out.println("  - User: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        
        List<Salesperson> salespeople = salespersonService.getSalespeopleByDivision(divisionId);
        List<SalespersonResponse> responses = salespeople.stream()
            .map(SalespersonResponse::new)
            .collect(Collectors.toList());
        
        System.out.println("✅ GET SALESPEOPLE BY DIVISION - Found: " + responses.size() + " salespeople");
        return ResponseEntity.ok(responses);
    }
    
    // Endpoint untuk get active salespeople saja berdasarkan division
    @GetMapping("/active/by-division/{divisionId}")
    @PreAuthorize(HAS_ACCESS_TO_PEMASARAN)
    public ResponseEntity<List<SalespersonResponse>> getActiveSalespeopleByDivision(@PathVariable Integer divisionId) {
        List<Salesperson> salespeople = salespersonService.getActiveSalespeopleByDivision(divisionId);
        List<SalespersonResponse> responses = salespeople.stream()
            .map(SalespersonResponse::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize(HAS_ACCESS_TO_PEMASARAN)
    public ResponseEntity<SalespersonResponse> updateSalesperson(@PathVariable Integer id, @Valid @RequestBody SalespersonRequest request) {
        Salesperson salesperson = salespersonService.updateSalesperson(id, request);
        return ResponseEntity.ok(new SalespersonResponse(salesperson));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize(HAS_ACCESS_TO_PEMASARAN)
    public ResponseEntity<Void> deleteSalesperson(@PathVariable Integer id) {
        salespersonService.deleteSalesperson(id);
        return ResponseEntity.noContent().build();
    }
}