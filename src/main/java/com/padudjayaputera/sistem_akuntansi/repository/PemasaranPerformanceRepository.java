package com.padudjayaputera.sistem_akuntansi.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.PemasaranPerformance;

@Repository
public interface PemasaranPerformanceRepository extends JpaRepository<PemasaranPerformance, Integer> {
    
    List<PemasaranPerformance> findByTanggalLaporan(LocalDate tanggal);
    
    List<PemasaranPerformance> findBySalesPerson(String salesPerson);
    
    @Query("SELECT pp FROM PemasaranPerformance pp WHERE pp.tanggalLaporan BETWEEN :startDate AND :endDate")
    List<PemasaranPerformance> findByTanggalLaporanBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT pp FROM PemasaranPerformance pp WHERE pp.entriHarian.account.division.id = :divisionId")
    List<PemasaranPerformance> findByDivisionId(@Param("divisionId") Integer divisionId);
}
