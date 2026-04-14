package com.imageprocessing.processing_service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.imageprocessing.processing_service.model.ImageUpload;

public interface ImageProcessingService extends IService<ImageUpload> {

    void process(ImageUpload event) throws Exception;

}
