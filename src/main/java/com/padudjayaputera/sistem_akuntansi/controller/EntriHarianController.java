package com.padudjayaputera.sistem_akuntansi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.padudjayaputera.sistem_akuntansi.dto.EntriHarianRequest;
import com.padudjayaputera.sistem_akuntansi.model.EntriHarian;
import com.padudjayaputera.sistem_akuntansi.service.EntriHarianService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/entri-harian")
@RequiredArgsConstructor
public class EntriHarianController {

    private final EntriHarianService entriHarianService;

    @PostMapping("/batch")
    @PreAuthorize("isAuthenticated()") // Hanya user yang sudah login yang bisa mengakses
    public ResponseEntity<String> createBatchEntries(@RequestBody List<EntriHarianRequest> requests) {
        entriHarianService.saveBatchEntries(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body("Data laporan harian berhasil disimpan.");
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EntriHarian> updateEntry(@PathVariable Integer id, @RequestBody EntriHarianRequest request) {
        EntriHarian updatedEntry = entriHarianService.updateEntry(id, request);
        return ResponseEntity.ok(updatedEntry);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteEntry(@PathVariable Integer id) {
        entriHarianService.deleteEntry(id);
        return ResponseEntity.noContent().build();
    }
}