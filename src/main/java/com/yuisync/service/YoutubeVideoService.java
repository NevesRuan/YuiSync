package com.yuisync.service;

import com.yuisync.model.DTOs.YoutubeVideoMetadataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Slf4j
@Service
public class YoutubeVideoService implements VideoPlatform {

    @Value("${yuisync.download.path}")
    private String downloadPath;

    // Check if yt-dlp is available in PATH
    public Boolean checkPath() {
    String pathValue = System.getenv("PATH");

    Optional<String> optionalPath = Optional.ofNullable(pathValue);

    // Check if PATH is present and not empty, then check for yt-dlp or yt-dl
    if (optionalPath.isPresent() && !optionalPath.get().isEmpty()) {
        if (optionalPath.get()
            .lines()
        .noneMatch(entry -> entry.contains("yt-dlp") || entry.contains("yt-dl"))) {
            System.out.println("yt-dlp or yt-dl not found in PATH environment variable.");
            return false;
        } else{
            System.out.println("yt-dlp or yt-dl found in PATH environment variable.");
            return true;
        }
    } else {
            log.warn("The PATH environment variable is NOT set or is empty.");
            return false;
        }
    }


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

    @Override
    public YoutubeVideoMetadataDTO metadata(String url) throws IOException {
        YoutubeVideoMetadataDTO youtubeVideoMetadataDTO = extractMetadata(url);
        return youtubeVideoMetadataDTO;
    }

    private YoutubeVideoMetadataDTO extractMetadata(String url) {
        log.info("Checking environment variables for yt-dlp on PATH:");
        // Check if yt-dlp is available in PATH
        if (checkPath().booleanValue()) {
            log.info("yt-dlp is available in PATH. Proceeding with the Meta Datas.");

            // If yt-dlp is available, start extracting metadata using yt-dlp
            log.info("Getting Meta Datas on YoutubeVideoService: {}", url);

            ProcessBuilder dataProcessBuilder = new ProcessBuilder(
                    "yt-dlp",
                    "-j",
                    "--no-warnings",
                    url
            );

            try {
                Process process = dataProcessBuilder.start();

                //reading JSON output file
                StringBuilder jsonOutput = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonOutput.append(line);
                    }
                }

                //waiting exit
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    String errorMsg = new String(process.getErrorStream().readAllBytes());
                    throw new RuntimeException("Error on metadata extraction. Exit code: " + exitCode + ". Erro: " + errorMsg);
                }

                //  JSON -> Java Object (Jackson)
                ObjectMapper mapper = new ObjectMapper();

                return mapper.readValue(jsonOutput.toString(), YoutubeVideoMetadataDTO.class);

            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Fail on yt-dlp execution fot metadata", e);
            }   
        } else {
            // If yt-dlp is not available, log an error and throw an exception
            log.error("yt-dlp is not available in PATH. Please install yt-dlp and ensure it's added to your system's PATH.");
            throw new RuntimeException("yt-dlp is not available in PATH. Please install yt-dlp and ensure it's added to your system's PATH.");
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