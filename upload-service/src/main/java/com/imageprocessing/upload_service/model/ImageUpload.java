package com.imageprocessing.upload_service.model;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("image_upload")
public class ImageUpload {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String originalName;
    private String storedName;
    private String filePath;
    private String processedPath;
    private Long fileSize;
    private String mimeType;
    private String status;
    private Integer width;
    private Integer height;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
