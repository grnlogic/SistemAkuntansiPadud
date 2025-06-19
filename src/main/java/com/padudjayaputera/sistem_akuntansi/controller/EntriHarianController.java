package com.padudjayaputera.sistem_akuntansi.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.padudjayaputera.sistem_akuntansi.dto.EntriHarianRequest;
import com.padudjayaputera.sistem_akuntansi.model.EntriHarian;
import com.padudjayaputera.sistem_akuntansi.service.EntriHarianService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/entri-harian")
@RequiredArgsConstructor
@Slf4j
public class EntriHarianController {

    private final EntriHarianService entriHarianService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EntriHarian>> getAllEntries() {
        List<EntriHarian> entries = entriHarianService.getAllEntries();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EntriHarian>> getEntriesByDate(@PathVariable String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<EntriHarian> entries = entriHarianService.getEntriesByDate(localDate);
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/division/{divisionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EntriHarian>> getEntriesByDivision(@PathVariable Integer divisionId) {
        List<EntriHarian> entries = entriHarianService.getEntriesByDivision(divisionId);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EntriHarian> getEntryById(@PathVariable Integer id) {
        EntriHarian entry = entriHarianService.getEntryById(id);
        return ResponseEntity.ok(entry);
    }

    @PostMapping("/batch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createBatchEntries(@RequestBody List<EntriHarianRequest> requests) {
        try {
            log.info("=== BATCH ENTRY CREATION START ===");
            log.info("Raw request body: {}", requests);
            
            if (requests != null) {
                for (int i = 0; i < requests.size(); i++) {
                    EntriHarianRequest req = requests.get(i);
                    log.info("Request[{}]: accountId={}, tanggal={}, nilai={}, description='{}'", 
                            i, req.getAccountId(), req.getTanggal(), req.getNilai(), req.getDescription());
                }
            }
            
            if (requests == null || requests.isEmpty()) {
                log.error("Request is null or empty");
                return ResponseEntity.badRequest().body("Request tidak boleh kosong");
            }

            List<EntriHarian> savedEntries = entriHarianService.saveBatchEntries(requests);
            log.info("Successfully saved {} entries", savedEntries.size());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEntries);
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation error: " + e.getMessage());
        } catch (AccessDeniedException e) {
            log.error("Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Runtime error in batch creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in batch creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Terjadi kesalahan pada server: " + e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EntriHarian> createEntry(@RequestBody EntriHarianRequest request) {
        EntriHarian savedEntry = entriHarianService.saveEntry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEntry);
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