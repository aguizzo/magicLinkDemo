package fun.fest2.magicLinkDemo.controller;

import fun.fest2.magicLinkDemo.service.MagicLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MagicLinkController {
    @Autowired
    private final MagicLinkService magicLinkService;

    public MagicLinkController(MagicLinkService magicLinkService) {
        this.magicLinkService = magicLinkService;
    }

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

        String token = magicLinkService.generateMagicLink(email);
        model.addAttribute("message", "Magic link sent to your email");
        return "index";
    }

    // Form class for Thymeleaf binding
    public static class EmailForm {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}