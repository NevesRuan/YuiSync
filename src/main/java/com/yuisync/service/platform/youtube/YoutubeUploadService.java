package com.yuisync.service.platform.youtube;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.yuisync.model.Video;
import com.yuisync.model.enums.SocialPlatform;
import com.yuisync.service.platform.interfaces.VideoUploadable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class YoutubeUploadService implements VideoUploadable {

    private static final String APPLICATION_NAME = "YuiSync";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    // tokens are persisted here so the user only needs to authorize once
    private static final String TOKENS_DIRECTORY = "./tokens/youtube";
    private static final List<String> SCOPES = Collections.singletonList(YouTubeScopes.YOUTUBE_UPLOAD);

    @Value("${youtube.oauth2.client-id}")
    private String clientId;

    @Value("${youtube.oauth2.client-secret}")
    private String clientSecret;

    @Override
    public boolean supports(SocialPlatform platform) {
        return platform == SocialPlatform.YOUTUBE_SHORTS;
    }

    // Builds an authenticated YouTube client.
    // On first run, opens a browser for OAuth2 authorization.
    // Subsequent runs use the stored refresh token from TOKENS_DIRECTORY.
    private YouTube getYoutubeService() throws IOException, GeneralSecurityException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets()
                .setInstalled(new GoogleClientSecrets.Details()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY)))
                .setAccessType("offline")
                .build();

        // LocalServerReceiver opens a temporary local server on port 8888 to receive the OAuth2 callback
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Override
    public String upload(Video video) throws IOException {
        log.info("Starting YouTube upload for video: {}", video.getId());

        try {
            YouTube youtubeService = getYoutubeService();

            // Define the video snippet metadata
            VideoSnippet snippet = new VideoSnippet();
            snippet.setTitle(video.getTitle());
            snippet.setDescription(video.getDescription());
            snippet.setTags(List.of("yuisync", "shorts"));

            // Upload as private by default while there is still no interface. - user can change visibility in YouTube Studio
            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus("private");
            status.setSelfDeclaredMadeForKids(false);

            // Build the YouTube video resource
            com.google.api.services.youtube.model.Video youtubeVideo = new com.google.api.services.youtube.model.Video();
            youtubeVideo.setSnippet(snippet);
            youtubeVideo.setStatus(status);

            // Prepare the video file as a resumable media stream
            File videoFile = new File(video.getLocalFilePath());
            if (!videoFile.exists()) {
                throw new FileNotFoundException("Local video file not found: " + video.getLocalFilePath());
            }

            InputStreamContent mediaContent = new InputStreamContent(
                    "video/mp4",
                    new BufferedInputStream(new FileInputStream(videoFile))
            );
            mediaContent.setLength(videoFile.length());

            YouTube.Videos.Insert videoInsert = youtubeService.videos()
                    .insert(List.of("snippet", "status"), youtubeVideo, mediaContent);

            // Resumable upload is more reliable for larger files
            MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
            uploader.setDirectUploadEnabled(false);
            uploader.setProgressListener(uploadStatus ->
                    log.info("[YouTube Upload] State: {} - {}%",
                            uploadStatus.getUploadState(),
                            (int) (uploadStatus.getProgress() * 100)));

            com.google.api.services.youtube.model.Video returnedVideo = videoInsert.execute();

            String youtubeUrl = "https://www.youtube.com/shorts/" + returnedVideo.getId();
            log.info("Video uploaded successfully to YouTube: {}", youtubeUrl);
            return youtubeUrl;

        } catch (GeneralSecurityException e) {
            throw new IOException("Failed to establish secure connection with YouTube API", e);
        }
    }
}

