package com.padudjayaputera.sistem_akuntansi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.padudjayaputera.sistem_akuntansi.model.PiutangTransaksi;

@Repository
public interface PiutangTransaksiRepository extends JpaRepository<PiutangTransaksi, Integer> {

    // Di masa depan, kita bisa dengan mudah menambahkan method pencarian custom di sini.
    // Contoh:
    // List<PiutangTransaksi> findByTanggalTransaksiBetween(LocalDate awal, LocalDate akhir);

}