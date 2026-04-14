package com.imageprocessing.upload_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.file.*;

@Slf4j
@RestController // missing this
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    @Value("${app.storage.uploads-directory}")
    private String uploadsDir;

    @Value("${app.storage.processed-directory}")
    private String processedDir;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        log.info("Storage controller hit!");
        return ResponseEntity.ok("Storage controller is working!");
    }

    @GetMapping("/uploads/{filename}")
    public ResponseEntity<Resource> serveUpload(@PathVariable String filename) {
        return serveFile(uploadsDir, filename);
    }

    @GetMapping("/processed/{filename}")
    public ResponseEntity<Resource> serveProcessed(@PathVariable String filename) {
        return serveFile(processedDir, filename);
    }

    private ResponseEntity<Resource> serveFile(String directory, String filename) {
        try {
            Path filePath = Path.of(directory).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error serving file: {}/{}", directory, filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}