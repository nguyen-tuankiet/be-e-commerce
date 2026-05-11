package com.example.becommerce.service.impl;

import com.example.becommerce.constant.ErrorCode;
import com.example.becommerce.dto.response.upload.ImageUploadResponse;
import com.example.becommerce.dto.response.upload.ImagesUploadResponse;
import com.example.becommerce.exception.AppException;
import com.example.becommerce.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final Set<String> ALLOWED_FOLDERS = Set.of("avatars", "orders", "verifications", "categories");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    @Value("${app.upload.base-dir:uploads}")
    private String baseDir;

    @Value("${app.upload.public-url:/uploads}")
    private String publicUrl;

    @Override
    public ImageUploadResponse storeImage(MultipartFile file, String folder) {
        validate(file, folder);

        String extension = extensionOf(file.getOriginalFilename(), file.getContentType());
        String filename = UUID.randomUUID() + extension;
        Path destination = Path.of(baseDir).resolve(folder).resolve(filename).normalize();

        try {
            Files.createDirectories(destination.getParent());
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw AppException.badRequest(ErrorCode.FILE_UPLOAD_FAILED, "Không thể lưu file ảnh");
        }

        return ImageUploadResponse.builder()
                .url(publicUrl.replaceAll("/$", "") + "/" + folder + "/" + filename)
                .filename(filename)
                .size(file.getSize())
                .mimeType(file.getContentType())
                .build();
    }

    @Override
    public ImagesUploadResponse storeImages(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) {
            throw AppException.badRequest(ErrorCode.INVALID_FILE, "Vui lòng chọn ít nhất một ảnh");
        }
        return ImagesUploadResponse.builder()
                .urls(files.stream().map(file -> storeImage(file, folder).getUrl()).toList())
                .build();
    }

    private void validate(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw AppException.badRequest(ErrorCode.INVALID_FILE, "File ảnh không được để trống");
        }
        if (!StringUtils.hasText(folder) || !ALLOWED_FOLDERS.contains(folder.trim().toLowerCase(Locale.ROOT))) {
            throw AppException.badRequest(ErrorCode.INVALID_FILE_FOLDER, "Thư mục upload không hợp lệ");
        }
        if (!ALLOWED_MIME_TYPES.contains(file.getContentType())) {
            throw AppException.badRequest(ErrorCode.INVALID_FILE_TYPE, "Chỉ hỗ trợ file ảnh jpeg, png, webp hoặc gif");
        }
    }

    private String extensionOf(String originalFilename, String mimeType) {
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (ext.matches("\\.[a-z0-9]{1,8}")) {
                return ext;
            }
        }
        return switch (mimeType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }
}
