package com.OA.system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件服务 - 简化版
 */
@Service
public class FileService {
    @Autowired
    private SecureFileService secureFileService;
    /**
     * 获取指定目录下的所有文件名
     * @param directoryPath 目录路径
     * @return 文件名列表
     */
    public List<String> getFileNames(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        secureFileService.validatePath(String.valueOf(path));
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("目录不存在: " + directoryPath);
        }

        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("路径不是目录: " + directoryPath);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            List<String> fileNames = new ArrayList<>();
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    fileNames.add(entry.getFileName().toString());
                }
            }
            return fileNames;
        }
    }

    /**
     * 递归获取所有文件名（包括子目录）
     * @param directoryPath 目录路径
     * @return 文件名列表
     */
    public List<String> getAllFileNamesRecursively(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        secureFileService.validatePath(String.valueOf(path));
        return Files.walk(path)
                .filter(Files::isRegularFile)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
    }

    /**
     * 获取文件详细信息
     * @param directoryPath 目录路径
     * @return 文件信息列表
     */
    public List<FileInfo> getFileDetails(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        secureFileService.validatePath(String.valueOf(path));
        return Files.list(path)
                .filter(Files::isRegularFile)
                .map(p -> {
                    try {
                        return new FileInfo(
                                p.getFileName().toString(),
                                Files.size(p),
                                Files.getLastModifiedTime(p).toMillis(),
                                p.toString()
                        );
                    } catch (IOException e) {
                        throw new RuntimeException("读取文件信息失败: " + p, e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取完整路径的文件列表（递归）
     * @param directoryPath 目录路径
     * @return 完整路径列表
     */
    public List<String> getAllFilePaths(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        secureFileService.validatePath(String.valueOf(path));
        return Files.walk(path)
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .collect(Collectors.toList());
    }

    /**
     * 文件信息类
     */
    public static class FileInfo {
        private String name;
        private long size;
        private long lastModified;
        private String fullPath;

        public FileInfo(String name, long size, long lastModified, String fullPath) {
            this.name = name;
            this.size = size;
            this.lastModified = lastModified;
            this.fullPath = fullPath;
        }

        // Getters
        public String getName() { return name; }
        public long getSize() { return size; }
        public long getLastModified() { return lastModified; }
        public String getFullPath() { return fullPath; }

        // Setters
        public void setName(String name) { this.name = name; }
        public void setSize(long size) { this.size = size; }
        public void setLastModified(long lastModified) { this.lastModified = lastModified; }
        public void setFullPath(String fullPath) { this.fullPath = fullPath; }
    }
}
