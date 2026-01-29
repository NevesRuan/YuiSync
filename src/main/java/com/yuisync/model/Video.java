package com.yuisync.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.yuisync.model.enums.SocialPlatform;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "tb_videos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    @Id
    private String id;

    //id from root platform
    @Column(unique = true)
    private String originalId;

    private String title;
    private String description;

    private String localFilePath;
    private Boolean isDownloaded;

    //upload platforms
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "video_taget_platforms",  joinColumns = @JoinColumn(name = "video_id"))
    @Enumerated(EnumType.STRING)
    private Set<SocialPlatform> targetPlatforms;

    //video upload status
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "video_upload_status", joinColumns = @JoinColumn(name = "video_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "status")
    private Map<SocialPlatform, String> uploadStatus;

    private LocalDateTime createdAt;
}
