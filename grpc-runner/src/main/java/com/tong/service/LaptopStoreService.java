package com.tong.service;

import com.tong.proto.Filter;
import com.tong.proto.Laptop;
import io.grpc.Context;

public interface LaptopStoreService {

    void save(Laptop laptop) throws Exception;

    Laptop find(String id);

    void search(Context ctx, Filter filter, LaptopStreamService stream);
}

