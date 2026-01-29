package com.yuisync.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class YoutubeVideoService implements VideoPlatform {

    @Value("${yuisync.download.path}")
    private String downloadPath;

    @Override
    public boolean supports(String url) {
        return url.contains("youtube.com") || url.contains("youtu.be");
    }

    // download
    @Override
    public File download(String url) {
        log.info("Starting download on YoutubeVideoService: {}", url);
        prepareDownloadDirectory();

        String videoId = extractVideoId(url);

        String outputTemplate = downloadPath + File.separator + "%(id)s.%(ext)s";

        ProcessBuilder processBuilder = new ProcessBuilder(
                "yt-dlp",
                "-f", "mp4",
                "-o", outputTemplate,
                url
        );

        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[yt-dlp] " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("yt-dlp failed, code: " + exitCode);
            }

            File file = new File(downloadPath, videoId + ".mp4");
            if (!file.exists()) {
                throw new RuntimeException("Archive can`t be located after download: " + file.getAbsolutePath());
            }
            return file;

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error executing yt-dlp", e);
        }
    }

    private void prepareDownloadDirectory() {
        try {
            Path path = Paths.get(downloadPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Not possible to create directory", e);
        }
    }

    public String extractVideoId(String youtubeUrl) {
        if (youtubeUrl.contains("shorts/")) {
            return youtubeUrl.substring(youtubeUrl.indexOf("shorts/") + 7);
        }

        return youtubeUrl.substring(youtubeUrl.length() - 11);
    }
}