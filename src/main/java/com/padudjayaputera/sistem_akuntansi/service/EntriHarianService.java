package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;

import com.padudjayaputera.sistem_akuntansi.dto.EntriHarianRequest;
import com.padudjayaputera.sistem_akuntansi.model.EntriHarian;

public interface EntriHarianService {

    /**
     * Menyimpan beberapa entri harian sekaligus (batch).
     * @param requests daftar data entri harian dari frontend.
     * @return Daftar entri yang berhasil disimpan.
     */
    List<EntriHarian> saveBatchEntries(List<EntriHarianRequest> requests);

    /**
     * Memperbarui entri harian berdasarkan ID.
     * @param id ID dari entri yang akan diperbarui.
     * @param request data entri yang baru.
     * @return Entri yang telah diperbarui.
     */
    EntriHarian updateEntry(Integer id, EntriHarianRequest request);

    /**
     * Menghapus entri harian berdasarkan ID.
     * @param id ID dari entri yang akan dihapus.
     */
    void deleteEntry(Integer id);
}