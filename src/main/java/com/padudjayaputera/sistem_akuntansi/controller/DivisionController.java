package com.padudjayaputera.sistem_akuntansi.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.padudjayaputera.sistem_akuntansi.model.Division;
import com.padudjayaputera.sistem_akuntansi.service.DivisionService;

@RestController
@RequestMapping("/api/v1/divisions")
public class DivisionController {

    private final DivisionService divisionService;

    public DivisionController(DivisionService divisionService) {
        this.divisionService = divisionService;
    }

    /**
     * Get all divisions
     */
    @GetMapping
    public ResponseEntity<List<Division>> getAllDivisions() {
        List<Division> divisions = divisionService.getAllDivisions();
        return ResponseEntity.ok(divisions);
    }

    /**
     * Create new division
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Division> createDivision(@RequestBody Division division) {
        Division createdDivision = divisionService.createDivision(division);
        return new ResponseEntity<>(createdDivision, HttpStatus.CREATED);
    }

    /**
     * Update division
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Division> updateDivision(@PathVariable Integer id, @RequestBody Division divisionDetails) {
        Division updatedDivision = divisionService.updateDivision(id, divisionDetails);
        return ResponseEntity.ok(updatedDivision);
    }

    /**
     * Delete division
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteDivision(@PathVariable Integer id) {
        divisionService.deleteDivision(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get division by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Division> getDivisionById(@PathVariable Integer id) {
        Division division = divisionService.getDivisionById(id);
        return ResponseEntity.ok(division);
    }
}