package com.example.becommerce.dto.response.upload;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ImagesUploadResponse {
    private final List<String> urls;
}
