package com.example.food.controller;


import com.example.food.service.FileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "File Display", description = "Rasmlarni ko'rish uchun proksi")
@Validated
public class FileDisplayController {

    private final FileService fileService;

    public FileDisplayController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/display")
    public ResponseEntity<InputStreamResource> displayFile(
            @RequestParam @NotBlank(message = "Fayl yo'li bo'sh bo'lmasligi kerak")
            @Pattern(regexp = "^(banners|products|categories)/.+\\.(jpg|png)$",
                    message = "Noto'g'ri fayl formati yoki yo'li")
            String path) {

        try {
            // 1. Path Traversal himoyasi (qo'shimcha xavfsizlik)
            if (path.contains("..") || path.startsWith("/") || path.contains(":")) {
                return ResponseEntity.badRequest().build();
            }

            InputStream inputStream = fileService.getFile(path);

            if (inputStream == null) {
                return ResponseEntity.notFound().build();
            }

            // 2. Dinamik Content-Type aniqlash (faqat JPEG emas, PNG ham bo'lishi mumkin)
            MediaType mediaType = determineMediaType(path);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header("Cache-Control", "public, max-age=31536000") // 1 yil kesh
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            // Agar MinIO xato bersa (fayl topilmasa), server "yiqilmaydi", shunchaki 404 qaytaradi
            return ResponseEntity.notFound().build();
        }
    }

    private MediaType determineMediaType(String path) {
        if (path.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (path.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }
}
