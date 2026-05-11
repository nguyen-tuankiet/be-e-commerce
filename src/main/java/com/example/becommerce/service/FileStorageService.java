package com.example.becommerce.service;

import com.example.becommerce.dto.response.upload.ImageUploadResponse;
import com.example.becommerce.dto.response.upload.ImagesUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {
    ImageUploadResponse storeImage(MultipartFile file, String folder);

    ImagesUploadResponse storeImages(List<MultipartFile> files, String folder);
}
