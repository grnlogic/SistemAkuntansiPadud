package com.padudjayaputera.sistem_akuntansi.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.GudangStok;

@Repository
public interface GudangStokRepository extends JpaRepository<GudangStok, Integer> {
    
    List<GudangStok> findByTanggalUpdate(LocalDate tanggal);
    
    List<GudangStok> findByBahanBakuName(String bahanBakuName);
    
    List<GudangStok> findByLokasiGudang(String lokasiGudang);
    
    @Query("SELECT gs FROM GudangStok gs WHERE gs.tanggalUpdate BETWEEN :startDate AND :endDate")
    List<GudangStok> findByTanggalUpdateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT gs FROM GudangStok gs WHERE gs.entriHarian.account.division.id = :divisionId")
    List<GudangStok> findByDivisionId(@Param("divisionId") Integer divisionId);
    
    // ✅ Helper untuk mendapatkan stok akhir terbaru per bahan baku
    @Query("SELECT gs FROM GudangStok gs WHERE gs.bahanBakuName = :bahanBakuName ORDER BY gs.tanggalUpdate DESC LIMIT 1")
    Optional<GudangStok> findLatestByBahanBakuName(@Param("bahanBakuName") String bahanBakuName);
}
