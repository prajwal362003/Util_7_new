package com.sbi.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sbi.dao.PerfiosStatusDao;

/**
 * Util 7: after the Perfios response zip is received, moves the CIFs present
 * in it from ZC to RR in kyc_perfios_customer_details.
 */
@Service
public class PerfiosStatusService {

    private static final Logger logger = (Logger) LogManager.getLogger(PerfiosStatusService.class);

    @Autowired
    private PerfiosStatusDao dao;

    /** Call once per successfully downloaded response zip. */
    public void markResponseReceived(File responseZip) {
        try {
            List<String> cifs = extractCifs(responseZip);
            if (cifs.isEmpty()) {
                logger.warn("No CIFs found in response zip: " + responseZip.getName());
                return;
            }
            int updated = dao.updateStatus(cifs);
            logger.info("Response " + responseZip.getName() + ": CIFs=" + cifs.size()
                    + ", ZC -> RR updated=" + updated);
        } catch (Exception e) {
            // never fail the download because of the status update; file is already saved
            logger.error("Status update failed for response zip: " + responseZip.getName(), e);
        }
    }

    /**
     * ASSUMPTION: entries inside the response zip are named cif_branch_account/...
     * (same folder naming as Util 9). Adjust parseCif() if Perfios response differs.
     */
    private List<String> extractCifs(File zip) throws Exception {
        Set<String> cifs = new LinkedHashSet<>();
        try (ZipFile zf = new ZipFile(zip)) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                String cif = parseCif(entries.nextElement().getName());
                if (cif != null) {
                    cifs.add(cif);
                }
            }
        }
        return new ArrayList<>(cifs);
    }

    private String parseCif(String entryName) {
        String first = entryName.replace('\\', '/').split("/")[0];
        String[] parts = first.split("_");
        if (parts.length >= 3 && parts[0].matches("\\d+")) {
            return parts[0];
        }
        return null;
    }
}
