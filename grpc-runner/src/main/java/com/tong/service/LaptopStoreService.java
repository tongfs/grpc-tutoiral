package com.tong.service;

import com.tong.proto.Laptop;

public interface LaptopStoreService {

    void save(Laptop laptop) throws Exception;

    Laptop find(String id);
}
