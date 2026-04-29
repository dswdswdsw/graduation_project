package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

public interface SysFileService extends IService<SysFile> {

    SysFile uploadFile(MultipartFile file, String bizType, Long bizId, Long userId);

    void deleteFile(Long id);

    SysFile getFileDetail(Long id);
}
