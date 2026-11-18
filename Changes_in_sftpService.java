=== sftpService.java ===

1) imports
import com.sbi.service.PerfiosStatusService;
import org.springframework.beans.factory.annotation.Autowired;

2) field (inside class)
@Autowired
private PerfiosStatusService perfiosStatusService;

3) in downloadFilesForLCPC(), replace
    if (downloaded) {
        manifest.setProperty(manifestKey, "DOWNLOADED");
with
    if (downloaded) {
        manifest.setProperty(manifestKey, "DOWNLOADED");
        // NEW: ZC -> RR for CIFs in this response (final file name has timestamp, see downloadSingleFile)
        perfiosStatusService.markResponseReceived(new File(localDirectory, lastSavedFileName));

4) downloadSingleFile() must expose the saved file name. Easiest: change its return to the Path
   (null on failure) -> then:
    Path saved = downloadSingleFile(...);
    if (saved != null) { ...; perfiosStatusService.markResponseReceived(saved.toFile()); }

=== properties file ===
UPDATE_STATUS_SQL=UPDATE kycupdate.kyc_perfios_customer_details SET cif_status = 'RR' WHERE cif_number = ? AND cif_status = 'ZC'
