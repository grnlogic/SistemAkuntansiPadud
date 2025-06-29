package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;

import com.padudjayaputera.sistem_akuntansi.dto.PiutangRequest;
import com.padudjayaputera.sistem_akuntansi.model.PiutangTransaksi;

public interface PiutangService {
    /**
     * Membuat transaksi piutang baru berdasarkan request dari frontend.
     * @param request Data piutang yang akan dibuat.
     * @return Objek PiutangTransaksi yang telah disimpan.
     */
    PiutangTransaksi createPiutang(PiutangRequest request);


    /**
     * Mengambil semua data transaksi piutang.
     * @return Daftar semua transaksi piutang.
     */
    List<PiutangTransaksi> getAllPiutang();

    /**
     * Mengubah data transaksi piutang yang sudah ada.
     * @param id ID dari transaksi yang akan diubah.
     * @param request Data baru dari frontend.
     * @return Objek PiutangTransaksi yang telah diperbarui.
     */
    PiutangTransaksi updatePiutang(Integer id, PiutangRequest request);

     /**
     * Menghapus transaksi piutang berdasarkan ID.
     * @param id ID dari transaksi yang akan dihapus.
     */
    void deletePiutang(Integer id);
}