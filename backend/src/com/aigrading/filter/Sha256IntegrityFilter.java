package com.aigrading.filter;

import com.aigrading.util.Sha256Util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Gateway Integrity Filter:
 * Intercepts submission artifacts (ZIP files), computes SHA-256 digest,
 * validates against Zip Slip path traversal exploits, and enforces artifact integrity.
 */
public class Sha256IntegrityFilter {

    public static class FilterResult {
        public boolean isAccepted;
        public String calculatedSha256;
        public String errorMessage;
        public byte[] validatedBytes;

        public FilterResult(boolean isAccepted, String calculatedSha256, String errorMessage, byte[] validatedBytes) {
            this.isAccepted = isAccepted;
            this.calculatedSha256 = calculatedSha256;
            this.errorMessage = errorMessage;
            this.validatedBytes = validatedBytes;
        }
    }

    /**
     * Inspects an uploaded ZIP payload at the gateway before delegating to the grading pipeline.
     */
    public static FilterResult inspectArtifact(byte[] zipBytes, String clientProvidedSha256) {
        if (zipBytes == null || zipBytes.length == 0) {
            return new FilterResult(false, null, "Gateway Filter: Empty artifact rejected. File size is 0 bytes.", null);
        }

        // 1. Minimum ZIP signature check (PK.. magic bytes: 0x50, 0x4B, 0x03, 0x04)
        if (zipBytes.length < 4 || zipBytes[0] != 0x50 || zipBytes[1] != 0x4B) {
            return new FilterResult(false, null, "Gateway Filter: Invalid artifact format. Expected valid ZIP file header.", null);
        }

        // 2. Compute SHA-256 Checksum
        String calculatedHash;
        try {
            calculatedHash = Sha256Util.calculateHash(zipBytes);
        } catch (Exception e) {
            return new FilterResult(false, null, "Gateway Filter: Failed to compute SHA-256 checksum: " + e.getMessage(), null);
        }

        // 3. Client Checksum verification (if provided)
        if (clientProvidedSha256 != null && !clientProvidedSha256.trim().isEmpty()) {
            if (!calculatedHash.equalsIgnoreCase(clientProvidedSha256.trim())) {
                return new FilterResult(false, calculatedHash,
                    String.format("Gateway Filter: SHA-256 checksum mismatch! Calculated: %s, Provided: %s. Artifact rejected.",
                        calculatedHash, clientProvidedSha256), null);
            }
        }

        // 4. Security Scan: Path Traversal (Zip Slip) Protection
        boolean safeZip = Sha256Util.isSafeZip(new ByteArrayInputStream(zipBytes));
        if (!safeZip) {
            return new FilterResult(false, calculatedHash,
                "Gateway Security Alert: Malicious path traversal (Zip Slip with '../') detected in artifact archive! Upload aborted.", null);
        }

        return new FilterResult(true, calculatedHash, null, zipBytes);
    }
}
