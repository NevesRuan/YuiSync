package com.yuisync.service.platform.interfaces;

import com.yuisync.model.DTOs.YoutubeVideoMetadataDTO;

import java.io.IOException;

public interface VideoMetadataFetchable {
    YoutubeVideoMetadataDTO metadata(String url) throws IOException;

}
