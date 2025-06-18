package com.padudjayaputera.sistem_akuntansi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class EntriHarianRequest {
    private Integer accountId;
    private LocalDate tanggal;
    private BigDecimal nilai;
}