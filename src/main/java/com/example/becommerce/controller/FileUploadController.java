package com.example.becommerce.controller;

import com.example.becommerce.dto.response.ApiResponse;
import com.example.becommerce.dto.response.upload.ImageUploadResponse;
import com.example.becommerce.dto.response.upload.ImagesUploadResponse;
import com.example.becommerce.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(path = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(
            @RequestParam MultipartFile file,
            @RequestParam String folder) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(fileStorageService.storeImage(file, folder)));
    }

    @PostMapping(path = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImagesUploadResponse>> uploadImages(
            @RequestParam List<MultipartFile> files,
            @RequestParam String folder) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(fileStorageService.storeImages(files, folder)));
    }
}
