package fun.fest2.magicLinkDemo.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MagicLinkService {
    // In-memory token storage (use database/Redis in production)
    private final Map<String, TokenRecord> tokens = new ConcurrentHashMap<>();

    private record TokenRecord(String email, long expiresAt) {}

    public String generateMagicLink(String email) {
        String token = RandomStringUtils.randomAlphanumeric(32);
        long expiresAt = Instant.now().plusSeconds(15 * 60).toEpochMilli(); // 15 min expiration
        tokens.put(token, new TokenRecord(email, expiresAt));

        // Simulate sending email (replace with actual email service)
        String link = "http://localhost:8080/upload?token=" + token;
        System.out.println("Magic link for " + email + ": " + link);
        return token;
    }

    public boolean validateToken(String token) {
        TokenRecord record = tokens.get(token);
        if (record == null || record.expiresAt() < Instant.now().toEpochMilli()) {
            tokens.remove(token);
            return false;
        }
        return true;
    }
}