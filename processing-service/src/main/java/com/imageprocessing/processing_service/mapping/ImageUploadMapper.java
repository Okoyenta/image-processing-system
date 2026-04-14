package com.imageprocessing.processing_service.mapping;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imageprocessing.processing_service.model.ImageUpload;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImageUploadMapper extends BaseMapper<ImageUpload> {

}

