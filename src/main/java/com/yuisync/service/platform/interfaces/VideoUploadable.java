package com.yuisync.service.platform.interfaces;

import com.yuisync.model.Video;
import com.yuisync.model.enums.SocialPlatform;

import java.io.IOException;

public interface VideoUploadable {
    boolean supports(SocialPlatform platform);
    String upload(Video video) throws IOException;
}
