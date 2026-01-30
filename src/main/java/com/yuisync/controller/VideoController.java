package com.yuisync.controller;

import com.yuisync.model.ProcessVideoRequestDTO;
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
    public ResponseEntity<Video> processVideo(@RequestBody ProcessVideoRequestDTO request) {
        Video video = videoService.processVideo(request.getUrl());
        return new ResponseEntity<>(video, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Video>> getAllVideos() {
        List<Video> videos = videoService.getAllVideos();
        return new ResponseEntity<>(videos, HttpStatus.OK);
    }

}
