package com.yuisync.model.DTOs;

import com.yuisync.model.enums.SocialPlatform;
import lombok.Data;

import java.util.Map;

@Data
public class UploadVideoResponseDTO {
    private String id;
    private String title;
    private Map<SocialPlatform, String> uploadStatus;
}
