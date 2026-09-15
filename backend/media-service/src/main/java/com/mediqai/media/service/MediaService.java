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
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "mediq-ai"
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
}