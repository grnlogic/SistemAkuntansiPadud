package com.padudjayaputera.sistem_akuntansi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.Salesperson;

@Repository
public interface SalespersonRepository extends JpaRepository<Salesperson, Integer> {
    
    // Find salesperson by division
    List<Salesperson> findByDivisionId(Integer divisionId);
    
    // Find active salesperson by division
    List<Salesperson> findByDivisionIdAndStatus(Integer divisionId, String status);
    
    // Find active salesperson
    List<Salesperson> findByStatus(String status);
}