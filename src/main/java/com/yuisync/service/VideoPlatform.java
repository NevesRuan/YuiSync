package com.yuisync.service;

import com.yuisync.model.DTOs.YoutubeVideoMetadataDTO;

import java.io.File;
import java.io.IOException;

public interface VideoPlatform {
    boolean supports(String url);
    File download(String url) throws IOException;
    YoutubeVideoMetadataDTO metadata(String url) throws IOException;
}
