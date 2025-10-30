package com.dev.quikkkk.user_service.service;

import org.springframework.web.multipart.MultipartFile;

public interface IFileStorageService {
    String uploadFile(MultipartFile file, String userId);

    void deleteFile(String fileUrl);

    boolean validateImage(MultipartFile file);
}
