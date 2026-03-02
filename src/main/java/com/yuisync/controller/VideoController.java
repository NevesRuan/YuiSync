package com.yuisync.controller;

import com.yuisync.model.DTOs.ProcessVideoRequestDTO;
import com.yuisync.model.DTOs.ProcessVideoResponseDTO;
import com.yuisync.model.DTOs.UploadVideoRequestDTO;
import com.yuisync.model.DTOs.UploadVideoResponseDTO;
import com.yuisync.model.Video;
import com.yuisync.service.VideoPersistenceService;
import com.yuisync.service.VideoProcessingService;
import com.yuisync.service.VideoUploadService;
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
    private final VideoUploadService videoUploadService;

    public VideoController(VideoProcessingService videoProcessingService, VideoPersistenceService videoPersistenceService, VideoUploadService videoUploadService) {
        this.videoProcessingService = videoProcessingService;
        this.videoPersistenceService = videoPersistenceService;
        this.videoUploadService = videoUploadService;
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

    @PostMapping("/{id}/upload")
    public ResponseEntity<UploadVideoResponseDTO> uploadVideo(
            @PathVariable String id,
            @RequestBody UploadVideoRequestDTO request
    ) throws IOException {
        UploadVideoResponseDTO responseDTO = videoUploadService.uploadVideo(id, request.getPlatforms());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

}
