package com.senexpert.userservice.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

/**
 * Service TOTP (RFC 6238) sans dependance externe.
 * Compatible Google Authenticator / Authy.
 */
@Service
public class TotpService {

    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int CODE_DIGITS = 6;
    private static final int TIME_STEP   = 30; // secondes
    private static final int WINDOW      = 1;  // tolerence +/- 1 fenetre

    // ─── Generation du secret ─────────────────────────────────────────────────

    /** Genere un secret aleatoire en Base32 (20 octets = 32 caracteres Base32) */
    public String genererSecret() {
        byte[] bytes = new byte[20];
        new SecureRandom().nextBytes(bytes);
        return encodeBase32(bytes);
    }

    /**
     * Construit l'URL otpauth pour scanner avec Google Authenticator.
     * Format : otpauth://totp/{issuer}:{account}?secret={secret}&issuer={issuer}
     */
    public String construireOtpauthUrl(String secret, String email) {
        String issuer = "SenExpertFormation";
        return "otpauth://totp/" + issuer + ":" + email
             + "?secret=" + secret
             + "&issuer=" + issuer
             + "&algorithm=SHA1&digits=6&period=30";
    }

    // ─── Verification ─────────────────────────────────────────────────────────

    /**
     * Verifie un code TOTP a 6 chiffres.
     * Accepte la fenetre courante +/- WINDOW fenetres (tolerance horloge).
     */
    public boolean verifierCode(String secret, String code) {
        if (secret == null || code == null || code.length() != CODE_DIGITS) return false;
        try {
            int inputCode = Integer.parseInt(code.trim());
            long currentWindow = Instant.now().getEpochSecond() / TIME_STEP;
            byte[] key = decodeBase32(secret);
            for (int i = -WINDOW; i <= WINDOW; i++) {
                if (computeTotp(key, currentWindow + i) == inputCode) return true;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }

    // ─── Calcul TOTP (HMAC-SHA1) ──────────────────────────────────────────────

    private int computeTotp(byte[] key, long timeWindow) {
        try {
            byte[] data = ByteBuffer.allocate(8).putLong(timeWindow).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "RAW"));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset]     & 0x7F) << 24)
                       | ((hash[offset + 1] & 0xFF) << 16)
                       | ((hash[offset + 2] & 0xFF) << 8)
                       |  (hash[offset + 3] & 0xFF);

            return binary % (int) Math.pow(10, CODE_DIGITS);
        } catch (Exception e) {
            throw new RuntimeException("Erreur calcul TOTP", e);
        }
    }

    // ─── Base32 ───────────────────────────────────────────────────────────────

    private String encodeBase32(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0, bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                sb.append(BASE32_CHARS.charAt((buffer >> bitsLeft) & 0x1F));
            }
        }
        if (bitsLeft > 0) sb.append(BASE32_CHARS.charAt((buffer << (5 - bitsLeft)) & 0x1F));
        return sb.toString();
    }

    private byte[] decodeBase32(String s) {
        s = s.toUpperCase().replaceAll("[^A-Z2-7]", "");
        int out = 0, bits = 0, idx = 0;
        byte[] result = new byte[s.length() * 5 / 8];
        for (char c : s.toCharArray()) {
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) continue;
            out = (out << 5) | val;
            bits += 5;
            if (bits >= 8) {
                bits -= 8;
                if (idx < result.length) result[idx++] = (byte) ((out >> bits) & 0xFF);
            }
        }
        return result;
    }
}
