package fun.fest2.magicLinkDemo.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class MagicLinkController {
    // In-memory token storage (use database/Redis in production)
    private final Map<String, TokenRecord> tokens = new ConcurrentHashMap<>();

    private record TokenRecord(String email, long expiresAt) {}

    // Serve magic link request page
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("emailForm", new EmailForm());
        model.addAttribute("message", null);
        return "index";
    }

    // Request magic link
    @PostMapping("/request-link")
    public String requestMagicLink(@ModelAttribute("emailForm") EmailForm emailForm, Model model) {
        String email = emailForm.getEmail();
        if (email == null || email.isEmpty()) {
            model.addAttribute("message", "Email required");
            return "index";
        }

        String token = RandomStringUtils.randomAlphanumeric(32);
        long expiresAt = Instant.now().plusSeconds(15 * 60).toEpochMilli(); // 15 min expiration
        tokens.put(token, new TokenRecord(email, expiresAt));

        // Simulate sending email (replace with actual email service)
        String link = "http://localhost:8080/upload?token=" + token;
        System.out.println("Magic link for " + email + ": " + link);
        model.addAttribute("message", "Magic link sent to your email");
        return "index";
    }

    // Serve CSV upload page with token validation
    @GetMapping("/upload")
    public String upload(@RequestParam(name = "token", required = false) String token, Model model) {
        if (token == null) {
            model.addAttribute("message", "No token provided");
            return "redirect:/";
        }

        TokenRecord record = tokens.get(token);
        if (record == null || record.expiresAt() < Instant.now().toEpochMilli()) {
            tokens.remove(token);
            model.addAttribute("message", "Invalid or expired link");
            return "redirect:/";
        }

        model.addAttribute("uploadForm", new UploadForm());
        model.addAttribute("message", null);
        return "upload";
    }

    // Handle CSV upload
    @PostMapping("/uploadFile")
    public String uploadFile(@ModelAttribute("uploadForm") UploadForm uploadForm, Model model) {
        MultipartFile file = uploadForm.getCsv();
        if (file == null || file.isEmpty()) {
            model.addAttribute("message", "No file uploaded");
            return "upload";
        }
        if (!file.getContentType().equals("text/csv")) {
            model.addAttribute("message", "File must be a CSV");
            return "upload";
        }

        // In production, save file to a secure location or process it
        System.out.println("Uploaded file: " + file.getOriginalFilename());
        model.addAttribute("message", "File " + file.getOriginalFilename() + " uploaded successfully");
        return "upload";
    }

    // Form classes for Thymeleaf binding
    public static class EmailForm {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class UploadForm {
        private MultipartFile csv;

        public MultipartFile getCsv() {
            return csv;
        }

        public void setCsv(MultipartFile csv) {
            this.csv = csv;
        }
    }

    // Keep /validate endpoint for potential API usage
    @GetMapping("/validate")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestParam String token) {
        TokenRecord record = tokens.get(token);
        if (record == null || record.expiresAt() < Instant.now().toEpochMilli()) {
            tokens.remove(token);
            return ResponseEntity.ok(Map.of("valid", false));
        }
        return ResponseEntity.ok(Map.of("valid", true));
    }
}