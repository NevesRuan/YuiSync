package com.yuisync.service;

import com.yuisync.model.DTOs.YoutubeVideoMetadataDTO;
import com.yuisync.service.platform.interfaces.VideoDownloadable;
import com.yuisync.service.platform.interfaces.VideoMetadataFetchable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoMetadataService {
    //the VideoService will choose the download base on the url the user send
    //for now i will implement only the Youtube source yt-dl

    private final List<VideoDownloadable> platforms;
    private final VideoMetadataFetchable metadataFetchable;

    public VideoDownloadable getStrategy(String url) {
        return platforms.stream()
                .filter(p -> p.supports(url))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Platform not supported: " + url));
    }

    public YoutubeVideoMetadataDTO fetchMetadata(String url) throws IOException {
        return metadataFetchable.metadata(url);
    }
}
