package com.yuisync.service;

import com.yuisync.model.DTOs.UploadVideoResponseDTO;
import com.yuisync.model.Video;
import com.yuisync.model.enums.SocialPlatform;
import com.yuisync.service.platform.interfaces.VideoUploadable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoUploadService {

    private final List<VideoUploadable> uploadPlatforms;
    private final VideoPersistenceService videoPersistenceService;

    public UploadVideoResponseDTO uploadVideo(String videoId, Set<SocialPlatform> targetPlatforms) throws IOException {
        Video video = videoPersistenceService.findById(videoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Video not found: " + videoId));

        if (!Boolean.TRUE.equals(video.getIsDownloaded())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Video is not downloaded yet: " + videoId);
        }

        for (SocialPlatform platform : targetPlatforms) {
            VideoUploadable uploader = uploadPlatforms.stream()
                    .filter(u -> u.supports(platform))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Upload not supported for platform: " + platform));

            try {
                log.info("Uploading video {} to platform {}", videoId, platform);
                String uploadUrl = uploader.upload(video);
                videoPersistenceService.updateUploadStatus(videoId, platform, "DONE: " + uploadUrl);
            } catch (IOException e) {
                log.error("Failed to upload video {} to platform {}: {}", videoId, platform, e.getMessage());
                // persist the error so the user can see what failed
                videoPersistenceService.updateUploadStatus(videoId, platform, "ERROR: " + e.getMessage());
            }
        }

        // Reload the updated video from DB to reflect all status changes
        Video updatedVideo = videoPersistenceService.findById(videoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        UploadVideoResponseDTO response = new UploadVideoResponseDTO();
        response.setId(updatedVideo.getId());
        response.setTitle(updatedVideo.getTitle());
        response.setUploadStatus(updatedVideo.getUploadStatus());
        return response;
    }
}

