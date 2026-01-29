package com.yuisync.service;

import com.yuisync.model.Video;
import com.yuisync.repository.VideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

@Slf4j
@Service
public class VideoService {

    private final VideoRepository videoRepository;

    @Value("${yuisync.download.path}")
    private String downloadPath;

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public Video processVideo(String videoURL) {
        String videoId = extractVideoId(videoURL);

        // checking for duplicates
        if (videoRepository.existsByOriginalId(videoId)) {
            log.warn("video {} already exists on the system. Ignoring it.", videoId);
            return videoRepository.findByOriginalId(videoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        }

        log.info("Starting video download: {}", videoId);
        prepareDownloadDirectory();

        // download
        File downloadedFile = executeYtDlp(videoURL, videoId);

        // saving on DB
        Video video = Video.builder()
                .id(UUID.randomUUID().toString())
                .originalId(videoId)
                .title("Download by YuiSync")
                .localFilePath(downloadedFile.getAbsolutePath())
                .isDownloaded(true)
                .targetPlatforms(new HashSet<>())
                .createdAt(LocalDateTime.now())
                .build();

        return videoRepository.save(video);
    }

    private File executeYtDlp(String url, String videoID){
        String outputTemplate = downloadPath + File.separator + "%(id)s.%(ext)s";

        ProcessBuilder processBuilder = new ProcessBuilder(
                "yt-dlp",
                "-f", "mp4",
                "-o", outputTemplate,
                url
        );

        processBuilder.redirectErrorStream(true);

        try{
            Process process = processBuilder.start();

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
                String line;
                while ((line = reader.readLine()) != null){
                    log.debug("[yt-dlp] " + line);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("yt-dlp failed with code: " + exitCode);
            }

            return new File(downloadPath, videoID + ".mp4");
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error to executing yt-dlp", e);
        }
    }

    private void  prepareDownloadDirectory() {
        try {
            Path path = Paths.get(downloadPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to create download directory", e);
        }
    }

    private String extractVideoId(String url) {
        //implementation starting with YouTube shorts
        if (url.contains("shorts/")){
            return url.substring(url.indexOf("shorts/") + 7);
        }
        return url.substring(url.length() - 11);
    }
}
