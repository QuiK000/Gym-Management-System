package com.dev.quikkkk.user_service.service.impl;

import com.dev.quikkkk.user_service.exception.BusinessException;
import com.dev.quikkkk.user_service.service.IFileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.dev.quikkkk.user_service.exception.ErrorCode.FILE_SIZE_EXCEEDED;
import static com.dev.quikkkk.user_service.exception.ErrorCode.FILE_UPLOAD_ERROR;
import static com.dev.quikkkk.user_service.exception.ErrorCode.INVALID_FILE_EXTENSION;
import static com.dev.quikkkk.user_service.exception.ErrorCode.INVALID_FILE_FORMAT;
import static com.dev.quikkkk.user_service.exception.ErrorCode.INVALID_IMAGE_DIMENSIONS;
import static com.dev.quikkkk.user_service.exception.ErrorCode.INVALID_IMAGE_FILE;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
@Slf4j
public class LocalFileStorageServiceImpl implements IFileStorageService {
    @Value("${app.storage.local.upload-dir}")
    private String uploadDir;

    @Value("${app.storage.local.base-url}")
    private String baseUrl;

    @Value("${app.avatar.allowed-extensions}")
    private String allowedExtensions;

    @Value("${app.avatar.max-size}")
    private long maxSize;

    @Value("${app.avatar.min-width}")
    private int minWidth;

    @Value("${app.avatar.max-width}")
    private int maxWidth;

    @Value("${app.avatar.min-height}")
    private int minHeight;

    @Value("${app.avatar.max-height}")
    private int maxHeight;

    @Override
    public String uploadFile(MultipartFile file, String userId) {
        log.info("Uploading avatar for user: {}", userId);

        if (!validateImage(file)) throw new BusinessException(INVALID_FILE_FORMAT);

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            String originalFilename = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalFilename);
            String filename = userId + "_" + UUID.randomUUID() + "." + extension;

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String fileUrl = baseUrl + "/uploads/avatars/" + filename;

            log.info("Avatar uploaded successfully: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("Failed to upload avatar for user: {}", userId, e);
            throw new BusinessException(FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return;

        try {
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir).resolve(filename);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted file: {}", filename);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
        }
    }

    @Override
    public boolean validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("File is empty");
            return false;
        }

        if (file.getSize() > maxSize) {
            log.warn("File size exceeds maximum: {} bytes", file.getSize());
            throw new BusinessException(FILE_SIZE_EXCEEDED);
        }

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        List<String> allowed = Arrays.asList(allowedExtensions.split(","));

        if (!allowed.contains(Objects.requireNonNull(extension).toLowerCase())) {
            log.warn("Invalid file extension: {}", extension);
            throw new BusinessException(INVALID_FILE_EXTENSION);
        }

        try {
            BufferedImage image = ImageIO.read(file.getInputStream());

            if (image == null) {
                log.warn("File is not a valid image");
                throw new BusinessException(INVALID_IMAGE_FILE);
            }

            int width = image.getWidth();
            int height = image.getHeight();

            if (width < minWidth || width > maxWidth || height < minHeight || height > maxHeight) {
                log.warn("Invalid image dimensions: {}x{}", width, height);
                throw new BusinessException(INVALID_IMAGE_DIMENSIONS);
            }

            return true;
        } catch (IOException e) {
            log.error("Failed to read image", e);
            throw new BusinessException(INVALID_IMAGE_FILE);
        }
    }
}
