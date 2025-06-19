package com.padudjayaputera.sistem_akuntansi.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.ProduksiHpp;

@Repository
public interface ProduksiHppRepository extends JpaRepository<ProduksiHpp, Integer> {
    
    List<ProduksiHpp> findByTanggalProduksi(LocalDate tanggal);
    
    List<ProduksiHpp> findByProdukName(String produkName);
    
    List<ProduksiHpp> findByOperator(String operator);
    
    @Query("SELECT ph FROM ProduksiHpp ph WHERE ph.tanggalProduksi BETWEEN :startDate AND :endDate")
    List<ProduksiHpp> findByTanggalProduksiBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT ph FROM ProduksiHpp ph WHERE ph.entriHarian.account.division.id = :divisionId")
    List<ProduksiHpp> findByDivisionId(@Param("divisionId") Integer divisionId);
}
