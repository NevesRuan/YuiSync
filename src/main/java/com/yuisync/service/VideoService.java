package com.yuisync.service;

import com.yuisync.model.DTOs.ProcessVideoResponseDTO;
import com.yuisync.model.DTOs.YoutubeVideoMetadataDTO;
import com.yuisync.model.Video;
import com.yuisync.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {
//the VideoService will choose the download base on the url the user send
//for now i will implement only the Youtube source yt-dl

    private final List<VideoPlatform> platforms;
    private final VideoRepository videoRepository;

    public ProcessVideoResponseDTO processVideo(String url) throws IOException {
        VideoPlatform strategy = platforms.stream()
                .filter(p -> p.supports(url))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Platform not supported: " + url));

        YoutubeVideoMetadataDTO metadataDTO = strategy.metadata(url);

        String videoId = metadataDTO.getVideoId();
        String videoTitle = metadataDTO.getVideoTitle();
        //Description with more than 255 char, will only work on MySQL. H2 database only accept VARCHAR(255)
        String videoDescription = metadataDTO.getVideoDescription();

        // checking for duplicates
        if (videoRepository.existsByOriginalId(videoId)) {
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

        File downloadedFile = strategy.download(url);

        // saving on DB
        Video video = Video.builder()
                .id(UUID.randomUUID().toString())
                .originalId(videoId)
                .title("Download by YuiSync - " + videoTitle) // try to pull the video name from the source
                .description("Download by YuiSync - " + videoDescription)
                .localFilePath(downloadedFile.getAbsolutePath())
                .isDownloaded(true)
                .targetPlatforms(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .build();

        videoRepository.save(video);

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

    public List<Video> getAllVideos() {
        log.info("Getting all videos from database...");

        List<Video> videos = videoRepository.findAll();
        return videos;
    }
}
