package com.yuisync.controller;

import com.yuisync.model.DTOs.ProcessVideoRequestDTO;
import com.yuisync.model.DTOs.ProcessVideoResponseDTO;
import com.yuisync.model.Video;
import com.yuisync.service.VideoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
public class VideoController {
    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @PostMapping("/process")
    public ResponseEntity<ProcessVideoResponseDTO> processVideo(
            @RequestBody ProcessVideoRequestDTO request
    ) {
        ProcessVideoResponseDTO responseDTO = videoService.processVideo(request.getUrl());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Video>> getAllVideos() {
        List<Video> video = videoService.getAllVideos();
        return new ResponseEntity<>(video, HttpStatus.OK);
    }

}
