package edu.example.springmvcdemo.model;

import jakarta.annotation.Nullable;
import java.util.Optional;
import static java.util.Objects.isNull;

public enum ImageExtension {
    GIF,
    JPG,
    JPEG,
    PNG;

    @Nullable
    public static ImageExtension isImage(String filename) {
        var extension = Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1))
                .orElse(null);
        if (isNull(extension)) {
            return null;
        }
        for (var ext : ImageExtension.values()) {
            if (ext.name().equalsIgnoreCase(extension)) {
                return ext;
            }
        }
        return null;
    }
}
