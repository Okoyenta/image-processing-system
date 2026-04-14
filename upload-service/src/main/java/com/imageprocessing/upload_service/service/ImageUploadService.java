package com.imageprocessing.upload_service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imageprocessing.upload_service.model.ImageUpload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageUploadService extends IService<ImageUpload> {

    ResponseEntity<ImageUpload> upload(MultipartFile file) throws IOException;
    ResponseEntity<ImageUpload> getById(Long id);
    ResponseEntity<List<ImageUpload>> getByStatus(String status);

}
