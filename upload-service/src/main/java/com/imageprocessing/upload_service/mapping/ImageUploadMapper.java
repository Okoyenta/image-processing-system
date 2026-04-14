package com.imageprocessing.upload_service.mapping;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.imageprocessing.upload_service.model.ImageUpload;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImageUploadMapper extends BaseMapper<ImageUpload> {

}
