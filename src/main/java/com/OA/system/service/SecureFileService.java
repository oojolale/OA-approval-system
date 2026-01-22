package com.OA.system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
public class SecureFileService {

    @Value("${file.base-directory}")
    private String baseDirectory;

    @Value("#{'${file.allowed-directories}'.split(',')}")
    private List<String> allowedDirectories;

    /**
     * 验证路径安全性
     */
    void validatePath(String requestedPath) {
        Path path = Paths.get(requestedPath).normalize();
        // 防止路径遍历攻击
        boolean isAllowed = false;
        for (String allowedDirectory : allowedDirectories) {
            isAllowed = path.startsWith(baseDirectory + allowedDirectory);
            if (isAllowed) {
                break;
            }
        }

        if (!isAllowed) {
            throw new SecurityException("不允许访问该目录");
        }
    }

    public List<String> getFileNames(String directoryPath) throws IOException {
        validatePath(directoryPath);
        // ... 其余代码
        return Files.list(Paths.get(directoryPath))
                .filter(Files::isRegularFile)
                .map(p -> p.getFileName().toString())
                .toList();
    }
}
