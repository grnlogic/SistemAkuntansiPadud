package com.padudjayaputera.sistem_akuntansi.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.padudjayaputera.sistem_akuntansi.dto.PerusahaanRequest;
import com.padudjayaputera.sistem_akuntansi.model.Perusahaan;
import com.padudjayaputera.sistem_akuntansi.repository.PerusahaanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PerusahaanServiceImpl implements PerusahaanService {

    private final PerusahaanRepository perusahaanRepository;

    @Override
    public Perusahaan createPerusahaan(PerusahaanRequest request) {
        Perusahaan perusahaan = new Perusahaan();
        perusahaan.setNama(request.getNama());
        return perusahaanRepository.save(perusahaan);
    }

    @Override
    public List<Perusahaan> getAllPerusahaan() {
        return perusahaanRepository.findAll();
    }

    @Override
    public Perusahaan getPerusahaanById(Integer id) {
        return perusahaanRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Perusahaan dengan ID " + id + " tidak ditemukan"));
    }

    @Override
    public Perusahaan updatePerusahaan(Integer id, PerusahaanRequest request) {
        Perusahaan perusahaan = getPerusahaanById(id);
        perusahaan.setNama(request.getNama());
        return perusahaanRepository.save(perusahaan);
    }

    @Override
    public void deletePerusahaan(Integer id) {
        Perusahaan perusahaan = getPerusahaanById(id);
        perusahaanRepository.delete(perusahaan);
    }
}
