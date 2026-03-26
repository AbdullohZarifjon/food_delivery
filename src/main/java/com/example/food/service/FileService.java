package com.example.food.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileService {
    String uploadFile(MultipartFile file, String folder);

    void deleteFile(String fileUrl);

    InputStream getFile(String decodedUrl);
}