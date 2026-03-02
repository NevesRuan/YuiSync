package com.yuisync.model.DTOs;

import com.yuisync.model.enums.SocialPlatform;
import lombok.Data;

import java.util.Set;

@Data
public class UploadVideoRequestDTO {
    private Set<SocialPlatform> platforms;
}
