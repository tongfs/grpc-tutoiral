package com.tong.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public interface ImageStoreService {
    String save(String laptopId, String imageType, ByteArrayOutputStream imageData) throws IOException;
}
