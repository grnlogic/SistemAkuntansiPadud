package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.padudjayaputera.sistem_akuntansi.model.Division;
import com.padudjayaputera.sistem_akuntansi.repository.DivisionRepository;

@Service
public class DivisionService {

    private final DivisionRepository divisionRepository;

    public DivisionService(DivisionRepository divisionRepository) {
        this.divisionRepository = divisionRepository;
    }

    public List<Division> getAllDivisions() {
        return divisionRepository.findAll();
    }

    public Division getDivisionById(Integer id) {
        return divisionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Division not found with id: " + id));
    }

    public Division createDivision(Division division) {
        return divisionRepository.save(division);
    }

    public Division updateDivision(Integer id, Division divisionDetails) {
        Division division = getDivisionById(id);
        division.setName(divisionDetails.getName());
        return divisionRepository.save(division);
    }

    public void deleteDivision(Integer id) {
        Division division = getDivisionById(id);
        divisionRepository.delete(division);
    }
}