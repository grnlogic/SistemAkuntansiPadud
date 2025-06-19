-- ✅ ALTERNATIVE: Remove unique constraint to allow multiple entries per day
ALTER TABLE entri_harian DROP CONSTRAINT IF EXISTS entri_harian_tanggal_laporan_account_id_key;

-- ✅ Add partial unique constraint only for non-specialized divisions (optional)
-- CREATE UNIQUE INDEX idx_entri_harian_unique_general 
-- ON entri_harian (tanggal_laporan, account_id) 
-- WHERE transaction_type IS NULL AND target_amount IS NULL AND hpp_amount IS NULL;
