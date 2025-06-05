package fun.fest2.magicLinkDemo.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class MagicLinkController {
    // In-memory token storage (use database/Redis in production)
    private final Map<String, TokenRecord> tokens = new ConcurrentHashMap<>();

    private record TokenRecord(String email, long expiresAt) {}

    // Request magic link
    @PostMapping("/request-link")
    public ResponseEntity<Map<String, String>> requestMagicLink(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email required"));
        }

        String token = RandomStringUtils.randomAlphanumeric(32);
        long expiresAt = Instant.now().plusSeconds(15 * 60).toEpochMilli(); // 15 min expiration
        tokens.put(token, new TokenRecord(email, expiresAt));

        // Simulate sending email (replace with actual email service)
        String link = "http://localhost:8080/upload.html?token=" + token;
        System.out.println("Magic link for " + email + ": " + link);
        return ResponseEntity.ok(Map.of("message", "Magic link sent to your email"));
    }

    // Validate token
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestParam String token) {
        TokenRecord record = tokens.get(token);
        if (record == null || record.expiresAt() < Instant.now().toEpochMilli()) {
            tokens.remove(token);
            return ResponseEntity.ok(Map.of("valid", false));
        }
        return ResponseEntity.ok(Map.of("valid", true));
    }

    // Handle CSV upload
    @PostMapping("/uploadFile")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("csv") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No file uploaded"));
        }
        // Validate file
        if (!file.getContentType().equals("text/csv")) {
            return ResponseEntity.badRequest().body(Map.of("message", "File must be a CSV"));
        }

        // In production, save file to a secure location or process it
        System.out.println("Uploaded file: " + file.getOriginalFilename());
        return ResponseEntity.ok(Map.of("message", "File " + file.getOriginalFilename() + " uploaded successfully"));
    }
}