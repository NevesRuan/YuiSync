package com.yuisync.service.platform.interfaces;

import com.yuisync.model.DTOs.YoutubeVideoMetadataDTO;

import java.io.File;
import java.io.IOException;

public interface VideoDownloadable {
    boolean supports(String url);
    File download(String url) throws IOException;
}
