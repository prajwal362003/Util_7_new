package com.sbi.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.sbi.config.ExtractProperties;

@Repository
public class PerfiosStatusDao {

    // UPDATE kycupdate.kyc_perfios_customer_details SET cif_status = 'RR' WHERE cif_number = ? AND cif_status = 'ZC'
    private static final String UPDATE_STATUS_SQL =
            ExtractProperties.getInstance().getProperty("UPDATE_STATUS_SQL");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** Moves CIFs from ZC to RR (statuses are in the SQL). Returns number of rows actually updated. */
    public int updateStatus(List<String> cifNumbers) {
        if (cifNumbers == null || cifNumbers.isEmpty()) {
            return 0;
        }
        int updated = 0;
        int[][] result = jdbcTemplate.batchUpdate(UPDATE_STATUS_SQL, cifNumbers, 1000,
                (ps, cif) -> ps.setString(1, cif));
        for (int[] batch : result) {
            for (int r : batch) {
                // some drivers return SUCCESS_NO_INFO (-2) for a successful row
                updated += (r == java.sql.Statement.SUCCESS_NO_INFO) ? 1 : Math.max(r, 0);
            }
        }
        return updated;
    }
}
