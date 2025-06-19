package com.padudjayaputera.sistem_akuntansi.model;

public enum TransactionType {
    PENERIMAAN("Penerimaan"),
    PENGELUARAN("Pengeluaran");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}