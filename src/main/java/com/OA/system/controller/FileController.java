package com.OA.system.controller;

import com.OA.system.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 获取指定目录的文件列表
     * 示例: GET /api/files/list?path=/home/user/documents
     */
    @GetMapping("/list")
    public Map<String, Object> listFiles(@RequestParam String path) {

        Map<String, Object> result = new HashMap<>();

        try {
            List<String> files = fileService.getFileNames(path);
            result.put("success", true);
            result.put("data", files);
            result.put("count", files.size());
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "读取目录失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/list-path")
    public Map<String, Object> listPathFiles(@RequestParam String path) {

        Map<String, Object> result = new HashMap<>();

        try {
            List<String> files = fileService.getAllFilePaths(path);
            result.put("success", true);
            result.put("data", files);
            result.put("count", files.size());
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "读取目录失败: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    /**
     * 递归获取所有文件
     * 示例: GET /api/files/list-all?path=/home/user/documents
     */
    @GetMapping("/list-all")
    public Map<String, Object> listAllFiles(@RequestParam String path) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> files = fileService.getAllFileNamesRecursively(path);
            result.put("success", true);
            result.put("data", files);
            result.put("count", files.size());
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "读取目录失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取文件详细信息
     * 示例: GET /api/files/details?path=/home/user/documents
     */
    @GetMapping("/details")
    public Map<String, Object> getFileDetails(@RequestParam String path) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<FileService.FileInfo> fileInfos = fileService.getFileDetails(path);
            result.put("success", true);
            result.put("data", fileInfos);
            result.put("count", fileInfos.size());
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "读取目录失败: " + e.getMessage());
        }
        return result;
    }
}
