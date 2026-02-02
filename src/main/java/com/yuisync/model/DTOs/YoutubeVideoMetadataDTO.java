package com.yuisync.model.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class YoutubeVideoMetadataDTO {

    @JsonProperty("id")
    String videoId;
    @JsonProperty("title")
    private String videoTitle;
    @JsonProperty("description")
    private String videoDescription;
}
