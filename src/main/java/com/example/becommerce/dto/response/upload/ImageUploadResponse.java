package com.example.becommerce.dto.response.upload;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageUploadResponse {
    private final String url;
    private final String filename;
    private final long size;
    private final String mimeType;
}
