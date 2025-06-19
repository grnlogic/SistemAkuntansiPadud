package com.padudjayaputera.sistem_akuntansi.service;

import java.time.LocalDate;
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
     * Menyimpan satu entri harian.
     * @param request data entri harian dari frontend.
     * @return Entri yang berhasil disimpan.
     */
    EntriHarian saveEntry(EntriHarianRequest request);

    /**
     * Mendapatkan semua entri harian.
     * @return Daftar semua entri harian.
     */
    List<EntriHarian> getAllEntries();

    /**
     * Mendapatkan entri harian berdasarkan tanggal.
     * @param date tanggal yang dicari.
     * @return Daftar entri harian pada tanggal tersebut.
     */
    List<EntriHarian> getEntriesByDate(LocalDate date);

    /**
     * Mendapatkan entri harian berdasarkan divisi.
     * @param divisionId ID divisi.
     * @return Daftar entri harian untuk divisi tersebut.
     */
    List<EntriHarian> getEntriesByDivision(Integer divisionId);

    /**
     * Mendapatkan entri harian berdasarkan ID.
     * @param id ID entri harian.
     * @return Entri harian yang ditemukan.
     */
    EntriHarian getEntryById(Integer id);

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