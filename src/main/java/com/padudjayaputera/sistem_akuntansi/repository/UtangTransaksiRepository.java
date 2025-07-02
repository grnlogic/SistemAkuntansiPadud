package com.padudjayaputera.sistem_akuntansi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.UtangTransaksi;

@Repository
public interface UtangTransaksiRepository extends JpaRepository<UtangTransaksi, Integer> {
    /**
     * Cari utang berdasarkan user ID
     */
    @Query("SELECT u FROM UtangTransaksi u WHERE u.user.id = :userId")
    List<UtangTransaksi> findByUserId(@Param("userId") Integer userId);
}