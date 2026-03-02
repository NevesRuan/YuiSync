package com.yuisync.service;

import com.yuisync.model.DTOs.ProcessVideoResponseDTO;
import com.yuisync.model.Video;
import com.yuisync.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoPersistenceService {

    private final VideoRepository videoRepository;

    public boolean existsByOriginalId(String videoId) {
        return videoRepository.existsByOriginalId(videoId);
    }

    public ProcessVideoResponseDTO findAlreadyProcessed(String videoId) {
        log.warn("Video {} already exist. Returning the DB.", videoId);

        ProcessVideoResponseDTO alreadyProcess = new ProcessVideoResponseDTO();

        Video processVideo = videoRepository.findByOriginalId(videoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        alreadyProcess.setId(processVideo.getId());
        alreadyProcess.setTitle(processVideo.getTitle());
        alreadyProcess.setDescription(processVideo.getDescription());
        alreadyProcess.setIsDownloaded(processVideo.getIsDownloaded());
        alreadyProcess.setTargetPlatforms(processVideo.getTargetPlatforms());
        alreadyProcess.setUploadStatus(processVideo.getUploadStatus());
        alreadyProcess.setCreatedAt(processVideo.getCreatedAt());

        return alreadyProcess;
    }

    public Video saveVideo(String videoId, String videoTitle, String videoDescription, String localFilePath) {
        // saving on DB
        Video video = Video.builder()
                .id(UUID.randomUUID().toString())
                .originalId(videoId)
                .title("Download by YuiSync - " + videoTitle)
                .description("Download by YuiSync - " + videoDescription)
                .localFilePath(localFilePath)
                .isDownloaded(true)
                .targetPlatforms(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .build();

        videoRepository.save(video);
        return video;
    }

    public List<Video> getAllVideos() {
        log.info("Getting all videos from database...");

        List<Video> videos = videoRepository.findAll();
        return videos;
    }
}
