package com.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.SysFile;
import com.campus.exception.BusinessException;
import com.campus.mapper.SysFileMapper;
import com.campus.service.SysFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements SysFileService {

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @Value("${file.max-size:10485760}")
    private Long maxSize;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/zip",
            "text/plain"
    );

    private static final List<String> DANGEROUS_EXTENSIONS = Arrays.asList(
            "exe", "bat", "cmd", "sh", "php", "jsp", "asp", "html", "js"
    );

    @Override
    public SysFile uploadFile(MultipartFile file, String bizType, Long bizId, Long userId) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > maxSize) {
            throw new BusinessException("文件大小超过限制");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("不支持的文件类型");
        }

        String originalName = file.getOriginalFilename();
        if (originalName != null) {
            String ext = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
            if (DANGEROUS_EXTENSIONS.contains(ext)) {
                throw new BusinessException("不允许上传该类型文件");
            }
        }

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String storedName = UUID.randomUUID().toString().replace("-", "")
                + (originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")) : "");
        String relativePath = datePath + "/" + storedName;
        String fullPath = uploadPath + "/" + relativePath;

        File dest = new File(fullPath);
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }

        SysFile sysFile = new SysFile();
        sysFile.setOriginalName(originalName);
        sysFile.setStoredName(storedName);
        sysFile.setFilePath(relativePath);
        sysFile.setFileSize(file.getSize());
        sysFile.setFileType(contentType);
        sysFile.setBizType(bizType);
        sysFile.setBizId(bizId);
        sysFile.setUploadUserId(userId);
        sysFile.setDeleted(0);
        save(sysFile);

        return sysFile;
    }

    @Override
    public void deleteFile(Long id) {
        SysFile sysFile = getById(id);
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }
        File file = new File(uploadPath + "/" + sysFile.getFilePath());
        if (file.exists()) {
            file.delete();
        }
        removeById(id);
    }

    @Override
    public SysFile getFileDetail(Long id) {
        SysFile sysFile = getById(id);
        if (sysFile == null) {
            throw new BusinessException("文件不存在");
        }
        return sysFile;
    }
}
