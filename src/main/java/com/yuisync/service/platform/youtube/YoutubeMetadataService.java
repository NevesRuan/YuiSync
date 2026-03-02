package com.yuisync.service.platform.youtube;

import com.yuisync.model.DTOs.YoutubeVideoMetadataDTO;
import com.yuisync.service.platform.interfaces.VideoMetadataFetchable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

@Slf4j
@Service
public class YoutubeMetadataService implements VideoMetadataFetchable {

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
    public YoutubeVideoMetadataDTO metadata(String url) throws IOException {
        YoutubeVideoMetadataDTO youtubeVideoMetadataDTO = extractMetadata(url);
        return youtubeVideoMetadataDTO;
    }

    private YoutubeVideoMetadataDTO extractMetadata(String url) {
        log.info("Checking environment variables for yt-dlp on PATH:");
        // Check if yt-dlp is available in PATH
        if (checkPath()) {
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
}
