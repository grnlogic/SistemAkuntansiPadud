package com.padudjayaputera.sistem_akuntansi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.Division;

@Repository
public interface DivisionRepository extends JpaRepository<Division, Integer> {
}