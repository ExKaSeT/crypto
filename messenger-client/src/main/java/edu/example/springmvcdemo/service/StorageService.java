package edu.example.springmvcdemo.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
@RequiredArgsConstructor
public class StorageService {

    @Value("${storage.dir}")
    private String storageDir;

    @PostConstruct
    public void setupPath() {
        try {
            Path path = Paths.get(storageDir);
            if (Files.notExists(path)) {
                Files.createDirectories(path);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create storage directory", e);
        }
    }

    private Path getFilePath(String filename) {
        return Path.of(storageDir, filename);
    }

    public OutputStream createNewFile(String filename) {
        Path filePath = getFilePath(filename);
        try {
            if (Files.exists(filePath)) {
                throw new IOException("File already exists: " + filename);
            }
            return Files.newOutputStream(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public InputStream getFile(String filename) {
        Path filePath = getFilePath(filename);
        try {
            if (!Files.exists(filePath)) {
                throw new IOException("File not found: " + filename);
            }
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteFile(String filename) {
        Path filePath = getFilePath(filename);
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void appendToFile(String filename, byte[] data) {
        Path filePath = getFilePath(filename);
        try {
            if (!Files.exists(filePath)) {
                throw new IOException("File not found: " + filename);
            }
            Files.write(filePath, data, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
