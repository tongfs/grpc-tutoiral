package com.tong.service;

import com.tong.exception.AlreadyExistsException;
import com.tong.proto.Filter;
import com.tong.proto.Laptop;
import com.tong.proto.Memory;
import io.grpc.Context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class LaptopStoreServiceImpl implements LaptopStoreService {

    private final Logger logger = Logger.getLogger(LaptopStoreServiceImpl.class.getName());

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

    /**
     * 通过 Filter 作为搜索条件搜索出符合条件的 Laptop
     */
    @Override
    public void search(Context ctx, Filter filter, LaptopStreamService stream) {
        for (Laptop laptop : data.values()) {
            if (ctx.isCancelled()) {
                logger.info("request is cancelled");
                return;
            }

//            Utils.heavyTask(1);

            if (isQualified(filter, laptop)) {
                stream.send(laptop.toBuilder().build());
            }
        }
    }

    private boolean isQualified(Filter filter, Laptop laptop) {
        if (laptop.getPriceUsd() > filter.getMaxPriceUsd()) {
            return false;
        }

        if (laptop.getCpu().getNumberCores() < filter.getMinCpuCores()) {
            return false;
        }

        if (laptop.getCpu().getMinGhz() < filter.getMinCpuGhz()) {
            return false;
        }

        if (toBit(laptop.getRam()) < toBit(filter.getMinRam())) {
            return false;
        }

        return true;
    }

    private long toBit(Memory memory) {
        long value = memory.getValue();

        switch (memory.getUnit()) {
            case BIT:
                return value;
            case BYTE:
                return value << 3;
            case KILOBYTE:
                return value << 13;
            case MEGABYTE:
                return value << 23;
            case GIGABYTE:
                return value << 33;
            case TERABYTE:
                return value << 43;
            default:
                return 0;
        }
    }
}
