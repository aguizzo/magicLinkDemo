package fun.fest2.magicLinkDemo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {
    public String handleFileUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file uploaded");
        }
        if (!file.getContentType().equals("text/csv")) {
            throw new IllegalArgumentException("File must be a CSV");
        }

        // In production, save file to a secure location or process it
        String fileName = file.getOriginalFilename();
        System.out.println("Uploaded file: " + fileName);
        return "File " + fileName + " uploaded successfully";
    }
}