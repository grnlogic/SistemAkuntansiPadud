package com.padudjayaputera.sistem_akuntansi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.Salesperson;

@Repository
public interface SalespersonRepository extends JpaRepository<Salesperson, Integer> {
}