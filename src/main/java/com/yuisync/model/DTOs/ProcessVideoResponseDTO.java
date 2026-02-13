package com.yuisync.model.DTOs;

import com.yuisync.model.enums.SocialPlatform;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Data
public class ProcessVideoResponseDTO {

    private String id;

    private String title;
    private String description;

    private Boolean isDownloaded;

    private Set<SocialPlatform> targetPlatforms;

    private Map<SocialPlatform, String> uploadStatus;

    private LocalDateTime createdAt;

}
