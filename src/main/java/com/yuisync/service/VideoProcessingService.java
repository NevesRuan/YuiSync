package com.yuisync.service;

import com.yuisync.model.DTOs.ProcessVideoResponseDTO;
import com.yuisync.model.DTOs.YoutubeVideoMetadataDTO;
import com.yuisync.model.Video;
import com.yuisync.service.platform.interfaces.VideoDownloadable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoProcessingService {

    private final VideoMetadataService videoMetadataService;
    private final VideoPersistenceService videoPersistenceService;

    public ProcessVideoResponseDTO processVideo(String url) throws IOException {
        VideoDownloadable strategy = videoMetadataService.getStrategy(url);

        YoutubeVideoMetadataDTO metadataDTO = videoMetadataService.fetchMetadata(url);

        String videoId = metadataDTO.getVideoId();
        String videoTitle = metadataDTO.getVideoTitle();
        //Description with more than 255 char, will only work on MySQL. H2 database only accept VARCHAR(255)
        String videoDescription = metadataDTO.getVideoDescription();

        // checking for duplicates
        if (videoPersistenceService.existsByOriginalId(videoId)) {
            return videoPersistenceService.findAlreadyProcessed(videoId);
        }

        File downloadedFile = strategy.download(url);

        // saving on DB
        Video video = videoPersistenceService.saveVideo(videoId, videoTitle, videoDescription, downloadedFile.getAbsolutePath());

        ProcessVideoResponseDTO  responseDTO = new  ProcessVideoResponseDTO();
        responseDTO.setId(video.getId());
        responseDTO.setTitle(video.getTitle());
        responseDTO.setDescription(video.getDescription());
        responseDTO.setIsDownloaded(video.getIsDownloaded());
        responseDTO.setTargetPlatforms(video.getTargetPlatforms());
        responseDTO.setUploadStatus(video.getUploadStatus());
        responseDTO.setCreatedAt(video.getCreatedAt());
        return responseDTO;
    }
}
