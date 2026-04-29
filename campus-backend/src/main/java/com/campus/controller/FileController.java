package com.campus.controller;

import com.campus.common.Result;
import com.campus.entity.SysFile;
import com.campus.service.SysFileService;
import com.campus.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final SysFileService sysFileService;
    private final JwtUtil jwtUtil;

    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<SysFile> uploadFile(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) Long bizId) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        SysFile sysFile = sysFileService.uploadFile(file, bizType, bizId, userId);
        return Result.success(sysFile);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        SysFile sysFile = sysFileService.getFileDetail(id);
        File file = new File(uploadPath + "/" + sysFile.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        FileSystemResource resource = new FileSystemResource(file);
        String encodedName = URLEncoder.encode(sysFile.getOriginalName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentType(MediaType.parseMediaType(sysFile.getFileType()))
                .contentLength(file.length())
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteFile(@PathVariable Long id) {
        sysFileService.deleteFile(id);
        return Result.success("删除成功", null);
    }

    @GetMapping("/{id}")
    public Result<SysFile> getFileDetail(@PathVariable Long id) {
        return Result.success(sysFileService.getFileDetail(id));
    }
}
