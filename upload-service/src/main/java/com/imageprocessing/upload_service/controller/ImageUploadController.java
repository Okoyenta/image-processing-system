package com.imageprocessing.upload_service.controller;


import com.imageprocessing.upload_service.kafka.ImageEventProducer;
import com.imageprocessing.upload_service.mapping.ImageUploadMapper;
import com.imageprocessing.upload_service.model.ImageUpload;
import com.imageprocessing.upload_service.service.ImageUploadService;
import com.imageprocessing.upload_service.rabbitmq.JobProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageUploadController {

    private final ImageUploadService uploadService;
    private  final JobProducer producer;


    @PostMapping("/upload")
    public ResponseEntity<ImageUpload> uploadImage(@RequestParam("file")MultipartFile file) throws Exception {
        // This method is intentionally left blank as the actual upload logic is handled in the service layer.
        // The controller will receive the file and pass it to the service for processing.
        return uploadService.upload(file);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ImageUpload> getImageById(@PathVariable Long id){
        return uploadService.getById(id);
    }

    @GetMapping
    public ResponseEntity<List<ImageUpload>> getImagesByStatus(@RequestParam(required = false)  String status){

        if(status == null || status.isEmpty()){
            return uploadService.getByStatus("UPLOADED");
        }

        return uploadService.getByStatus(status);
    }

    @PostMapping("/message")
    public ResponseEntity<String> sendMessage(@RequestParam String message) {
        try {
            producer.sendJob(message);
            return ResponseEntity.ok("Message sent successfully!");
        } catch (Exception e) {
            // Adding the exception to the log is a lifesaver for debugging
            System.err.println("Error sending to RabbitMQ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send message: " + e.getMessage());
        }
    }
}
