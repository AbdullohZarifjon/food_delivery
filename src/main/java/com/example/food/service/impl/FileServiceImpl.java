package com.example.food.service.impl;

import com.example.food.service.FileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.SneakyThrows;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    public FileServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

//    @Override
//    @SneakyThrows
//    public String uploadFile(MultipartFile file, String fileName) {
//        // 2. RAM'da rasmni siqish
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//        Thumbnails.of(file.getInputStream())
//                .size(1200, 1200) // Sifat uchun 1200 ideal
//                .outputFormat("jpg")
//                .outputQuality(0.75) // 75% - maksimal siqilish va zo'r sifat balansi
//                .toOutputStream(outputStream);
//
//        byte[] compressedBytes = outputStream.toByteArray();
//        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedBytes);
//
//        // 3. MinIO ga yuklash
//        minioClient.putObject(
//                PutObjectArgs.builder()
//                        .bucket(bucketName)
//                        .object(fileName)
//                        .stream(inputStream, compressedBytes.length, -1)
//                        .contentType("image/jpeg") // Brauzerlar WebP ni yaxshi taniydi
//                        .build()
//        );
//
//        return fileName;
//    }

    @Override
    @SneakyThrows
    public String uploadFile(MultipartFile file, String fileName) {
        // 1. Fayl turini aniqlaymiz (masalan: image/png, image/jpeg)
        String contentType = file.getContentType();

        // Default format va contentType
        String format = "jpg";
        String finalContentType = "image/jpeg";

        // 2. Agar PNG bo'lsa, formatni o'zgartirmaymiz
        if (contentType != null && contentType.contains("png")) {
            format = "png";
            finalContentType = "image/png";
        }

        // 3. RAM'da rasmni siqish
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(file.getInputStream())
                .size(1200, 1200)
                .outputFormat(format); // Dinamik format

        // JPG uchun sifatni pasaytirish (PNG'da sifatni pasaytirish boshqacha ishlaydi,
        // shuning uchun asosan JPG/JPEG uchun qo'llaymiz)
        if (format.equals("jpg")) {
            builder.outputQuality(0.75);
        }

        builder.toOutputStream(outputStream);

        byte[] compressedBytes = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedBytes);

        // 4. MinIO ga yuklash
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .stream(inputStream, compressedBytes.length, -1)
                        .contentType(finalContentType) // To'g'ri content type bilan saqlaymiz
                        .build()
        );

        return fileName;
    }


    @Override
    @SneakyThrows
    public InputStream getFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return null;

        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileUrl)
                        .build()
        );
    }


    @Override
    @SneakyThrows
    public void deleteFile(String filePath) { // fileUrl emas, filePath (banners/uuid.jpg) keladi
        if (filePath == null || filePath.isEmpty()) return;

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filePath) // "banners/7923e1ff...jpg"
                        .build()
        );
    }

}