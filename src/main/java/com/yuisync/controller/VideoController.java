package com.yuisync.controller;

import com.yuisync.model.DTOs.ProcessVideoRequestDTO;
import com.yuisync.model.DTOs.ProcessVideoResponseDTO;
import com.yuisync.model.Video;
import com.yuisync.service.VideoPersistenceService;
import com.yuisync.service.VideoProcessingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoController {
    private final VideoProcessingService videoProcessingService;
    private final VideoPersistenceService videoPersistenceService;

    public VideoController(VideoProcessingService videoProcessingService, VideoPersistenceService videoPersistenceService) {
        this.videoProcessingService = videoProcessingService;
        this.videoPersistenceService = videoPersistenceService;
    }

    @PostMapping("/process")
    public ResponseEntity<ProcessVideoResponseDTO> processVideo(
            @RequestBody ProcessVideoRequestDTO request
    ) throws IOException {
        ProcessVideoResponseDTO responseDTO = videoProcessingService.processVideo(request.getUrl());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Video>> getAllVideos() {
        List<Video> video = videoPersistenceService.getAllVideos();
        return new ResponseEntity<>(video, HttpStatus.OK);
    }

}
