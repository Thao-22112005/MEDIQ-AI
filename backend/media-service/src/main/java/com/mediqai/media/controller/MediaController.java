package com.mediqai.media.controller;

import com.mediqai.media.entity.Media;
import com.mediqai.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<Media> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") Long entityId
    ) throws Exception {

        Media media = mediaService.uploadImage(
                file,
                entityType,
                entityId
        );

        return ResponseEntity.ok(media);
    }
}