package com.yuisync.service;

import com.yuisync.model.Video;
import com.yuisync.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
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

    public Video processVideo(String url) {
        VideoPlatform strategy = platforms.stream()
                .filter(p -> p.supports(url))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Platform not supported: " + url));

        File downloadedFile = strategy.download(url);

        String videoId = downloadedFile.getName().replace(".mp4", "");

        // checking for duplicates
        if (videoRepository.existsByOriginalId(videoId)) {
            log.warn("Video {} already exist. Returning the DB.", videoId);
            return videoRepository.findByOriginalId(videoId).orElseThrow();
        }

        // saving on DB
        Video video = Video.builder()
                .id(UUID.randomUUID().toString())
                .originalId(videoId)
                .title("Download by YuiSync - " + videoId) // try to pull the video name from the source
                .localFilePath(downloadedFile.getAbsolutePath())
                .isDownloaded(true)
                .targetPlatforms(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .build();

        return videoRepository.save(video);
    }

    public List<Video> getAllVideos() {
        log.info("Getting all videos from database...");
        return videoRepository.findAll();
    }
}
