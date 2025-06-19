package com.padudjayaputera.sistem_akuntansi.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.EntriHarian;

@Repository
public interface EntriHarianRepository extends JpaRepository<EntriHarian, Integer> {
    
    /**
     * Mencari entri harian berdasarkan tanggal laporan.
     */
    List<EntriHarian> findByTanggalLaporan(LocalDate tanggal);
    
    /**
     * Mencari entri harian berdasarkan divisi dari akun.
     */
    @Query("SELECT e FROM EntriHarian e WHERE e.account.division.id = :divisionId")
    List<EntriHarian> findByAccountDivisionId(@Param("divisionId") Integer divisionId);
    
    /**
     * Mencari entri harian berdasarkan tanggal dan divisi.
     */
    @Query("SELECT e FROM EntriHarian e WHERE e.tanggalLaporan = :tanggal AND e.account.division.id = :divisionId")
    List<EntriHarian> findByTanggalLaporanAndAccountDivisionId(@Param("tanggal") LocalDate tanggal, @Param("divisionId") Integer divisionId);
}