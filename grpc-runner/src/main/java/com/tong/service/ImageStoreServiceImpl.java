package com.tong.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@AllArgsConstructor
public class ImageStoreServiceImpl implements ImageStoreService {

    private final ConcurrentMap<String, imageMetadata> data = new ConcurrentHashMap<>();

    private String imageFolder;

    @Override
    public String save(String laptopId, String imageType, ByteArrayOutputStream imageData) throws IOException {
        String imageId = UUID.randomUUID().toString();
        String imagePath = String.format("%s/%s%s", imageFolder, imageId, imageType);

        FileOutputStream fos = new FileOutputStream(imagePath);
        imageData.writeTo(fos);
        fos.close();

        imageMetadata metadata = new imageMetadata(laptopId, imageType, imagePath);
        data.put(imageId, metadata);

        return imageId;
    }

    @Getter
    @AllArgsConstructor
    private static class imageMetadata {
        private String laptopId;
        private String type;
        private String path;
    }
}
