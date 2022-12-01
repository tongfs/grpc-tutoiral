package com.tong.service;

import com.tong.proto.Laptop;

public interface LaptopStreamService {
    void send(Laptop laptop);
}
