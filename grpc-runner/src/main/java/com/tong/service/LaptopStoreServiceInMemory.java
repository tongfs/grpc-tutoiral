package com.tong.service;

import com.tong.exception.AlreadyExistsException;
import com.tong.proto.Laptop;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LaptopStoreServiceInMemory implements LaptopStoreService {

    private final Map<String, Laptop> data = new ConcurrentHashMap<>();

    /**
     * 存储 Laptop 对象
     */
    @Override
    public void save(Laptop laptop) throws Exception {
        if (data.containsKey(laptop.getId())) {
            throw new AlreadyExistsException("laptop ID already exists");
        }

        // deep copy
        Laptop other = laptop.toBuilder().build();
        data.put(other.getId(), other);
    }

    /**
     * 取出 Laptop 对象
     */
    @Override
    public Laptop find(String id) {
        if (!data.containsKey(id)) {
            return null;
        }

        // deep copy and return
        return data.get(id).toBuilder().build();
    }
}
