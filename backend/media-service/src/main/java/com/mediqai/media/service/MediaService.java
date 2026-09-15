package com.mediqai.media.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mediqai.media.entity.Media;
import com.mediqai.media.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final Cloudinary cloudinary;
    private final MediaRepository mediaRepository;

    public Media uploadImage(
            MultipartFile file,
            String entityType,
            Long entityId
    ) throws IOException {

        // 1. Kiểm tra file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // 2. Chỉ cho phép hình ảnh
        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException(
                    "Chỉ được upload file hình ảnh"
            );
        }

        // 3. Upload lên Cloudinary
        String folder = getUploadFolder(entityType);

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder
                )
        );

        // 4. Lấy thông tin Cloudinary trả về
        String url = (String) uploadResult.get("secure_url");
        String publicId = (String) uploadResult.get("public_id");

        // 5. Lưu metadata vào database
        Media media = Media.builder()
                .fileName(file.getOriginalFilename())
                .url(url)
                .publicId(publicId)
                .fileType(contentType)
                .entityType(entityType)
                .entityId(entityId)
                .createdAt(LocalDateTime.now())
                .build();

        return mediaRepository.save(media);
    }

    public List<Media> getMediaByEntity(
            String entityType,
            Long entityId
    ) {

        return mediaRepository.findByEntityTypeAndEntityId(
                entityType,
                entityId
        );
    }

    public void deleteMedia(Long id) throws IOException {

        // 1. Tìm media trong database
        Media media = mediaRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy media"));

        // 2. Xóa ảnh trên Cloudinary
        cloudinary.uploader().destroy(
                media.getPublicId(),
                ObjectUtils.emptyMap()
        );

        // 3. Xóa record trong MySQL
        mediaRepository.delete(media);
    }

    private String getUploadFolder(String entityType) {

        return switch (entityType.toUpperCase()) {
            case "PATIENT" -> "mediq-ai/patients";
            case "DOCTOR" -> "mediq-ai/doctors";
            case "CLINIC" -> "mediq-ai/clinics";
            default -> throw new IllegalArgumentException(
                    "Entity type không hợp lệ: " + entityType
            );
        };
    }

}