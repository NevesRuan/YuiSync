package com.yuisync.repository;

import com.yuisync.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, String> {

    boolean existsByOriginalId(String originalId);

    Optional<Video> findByOriginalId(String originalId);
}
