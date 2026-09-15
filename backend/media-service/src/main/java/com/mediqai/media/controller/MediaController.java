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

    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<?> getMediaByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId
    ) {

        return ResponseEntity.ok(
                mediaService.getMediaByEntity(entityType, entityId)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMedia(
            @PathVariable Long id
    ) throws Exception {

        mediaService.deleteMedia(id);

        return ResponseEntity.ok(
                "Xóa media thành công"
        );
    }
}