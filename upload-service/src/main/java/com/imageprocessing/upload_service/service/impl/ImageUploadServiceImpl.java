package com.imageprocessing.upload_service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imageprocessing.upload_service.kafka.ImageEventProducer;
import com.imageprocessing.upload_service.mapping.ImageUploadMapper;
import com.imageprocessing.upload_service.model.ImageUpload;
import com.imageprocessing.upload_service.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.nio.file.*;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Service
public class ImageUploadServiceImpl extends ServiceImpl<ImageUploadMapper, ImageUpload> implements ImageUploadService {

    @Value("${app.upload.directory}")
    private String uploadDirectory;

    private final ImageUploadMapper imageUploadMapper;
    private  final ImageEventProducer eventProducer;

    @Override
    public ResponseEntity<ImageUpload> upload(MultipartFile file) throws IOException {
        String ext        = getExtension(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + ext;
        Path   target     = Path.of(uploadDirectory, storedName);

        Files.createDirectories(target.getParent());
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        ImageUpload image = ImageUpload.builder()
                .originalName(file.getOriginalFilename())
                .storedName(storedName)
                .filePath(target.toString())
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .status("UPLOADED")
                .build();

        imageUploadMapper.insert(image);
        log.info("[Upload] Saved image id={} to {}", image.getId(), target);

        eventProducer.publish(ImageUpload.builder()
                .id(image.getId())
                .filePath(image.getFilePath())
                .originalName(image.getOriginalName())
                .mimeType(image.getMimeType())
                .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(image);
    }


    /**
     * @param id
     * @return
     */
    @Override
    public ResponseEntity<ImageUpload> getById(Long id) {
        ImageUpload image = imageUploadMapper.selectById(id);
        if(image == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(image);
    }

    /**
     * @param status
     * @return
     */
    @Override
    public ResponseEntity<List<ImageUpload>> getByStatus(String status) {
        List<ImageUpload> images = imageUploadMapper.selectList(new LambdaQueryWrapper<ImageUpload>().eq(ImageUpload::getStatus, status));

        if (images.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }

        return ResponseEntity.ok(images);
    }


    private String getExtension(String name) {
        if (name == null || !name.contains(".")) return ".bin";
        return name.substring(name.lastIndexOf('.'));
    }

}
