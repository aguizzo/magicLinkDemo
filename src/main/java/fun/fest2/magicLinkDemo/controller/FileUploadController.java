package fun.fest2.magicLinkDemo.controller;

import fun.fest2.magicLinkDemo.service.FileUploadService;
import fun.fest2.magicLinkDemo.service.MagicLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FileUploadController {
    private final MagicLinkService magicLinkService;
    private final FileUploadService fileUploadService;

    @Autowired
    public FileUploadController(MagicLinkService magicLinkService, FileUploadService fileUploadService) {
        this.magicLinkService = magicLinkService;
        this.fileUploadService = fileUploadService;
    }

    // Serve CSV upload page with token validation
    @GetMapping("/upload")
    public String upload(@RequestParam(name = "token", required = false) String token, Model model) {
        if (token == null) {
            model.addAttribute("message", "No token provided");
            return "redirect:/";
        }

        if (!magicLinkService.validateToken(token)) {
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
        try {
            String message = fileUploadService.handleFileUpload(file);
            model.addAttribute("message", message);
        } catch (IllegalArgumentException e) {
            model.addAttribute("message", e.getMessage());
        }
        return "upload";
    }

    // Form class for Thymeleaf binding
    public static class UploadForm {
        private MultipartFile csv;

        public MultipartFile getCsv() {
            return csv;
        }

        public void setCsv(MultipartFile csv) {
            this.csv = csv;
        }
    }
}