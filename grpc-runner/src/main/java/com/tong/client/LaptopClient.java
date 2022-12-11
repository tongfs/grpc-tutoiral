package com.tong.client;

import com.google.protobuf.ByteString;
import com.tong.proto.CreateLaptopRequest;
import com.tong.proto.CreateLaptopResponse;
import com.tong.proto.Filter;
import com.tong.proto.Laptop;
import com.tong.proto.LaptopServiceGrpc;
import com.tong.proto.Memory;
import com.tong.proto.SearchLaptopRequest;
import com.tong.proto.SearchLaptopResponse;
import com.tong.proto.UploadImageRequest;
import com.tong.proto.UploadImageResponse;
import com.tong.sample.Generator;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LaptopClient {
    private static final Logger logger = Logger.getLogger(LaptopClient.class.getName());

    private final ManagedChannel channel;
    private final LaptopServiceGrpc.LaptopServiceBlockingStub blockingStub;

    private final LaptopServiceGrpc.LaptopServiceStub asyncStub;

    private LaptopClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        blockingStub = LaptopServiceGrpc.newBlockingStub(channel);
        asyncStub = LaptopServiceGrpc.newStub(channel);
    }

    private void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    private void createLaptop(Laptop laptop) {
        CreateLaptopRequest request = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();
        CreateLaptopResponse response;
        try {
            response = blockingStub
                    .withDeadlineAfter(6, TimeUnit.SECONDS)
                    .createLaptop(request);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.ALREADY_EXISTS) {
                logger.info("laptop ID already exists");
                return;
            }
            logger.log(Level.SEVERE, "request failed: " + e.getMessage());
            return;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "request failed: " + e.getMessage());
            return;
        }

        logger.info("laptop created with ID: " + response.getId());
    }

    private void searchLaptop(Filter filter) {
        logger.info("search started");

        try {
            SearchLaptopRequest request = SearchLaptopRequest.newBuilder().setFilter(filter).build();
            Iterator<SearchLaptopResponse> iterator = blockingStub
                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                    .searchLaptop(request);

            while (iterator.hasNext()) {
                SearchLaptopResponse response = iterator.next();
                Laptop laptop = response.getLaptop();
                logger.info("- found: " + laptop.getId());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "request failed: " + e.getMessage());
            return;
        }

        logger.info("search completed");
    }

    private void uploadImage(String laptopId, String imagePath) throws InterruptedException {
        CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<UploadImageRequest> requestObserver = asyncStub.withDeadlineAfter(5, TimeUnit.SECONDS)
                .uploadImage(new StreamObserver<UploadImageResponse>() {
                    @Override
                    public void onNext(UploadImageResponse response) {
                        logger.info("receive response\n" + response);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        logger.log(Level.SEVERE, "upload failed: " + throwable);
                        finishLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("image uploaded");
                        finishLatch.countDown();
                    }
                });

        FileInputStream fis;
        try {
            fis = new FileInputStream(imagePath);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "cannot read image file: " + e.getMessage());
            return;
        }

        String imageType = imagePath.substring(imagePath.lastIndexOf("."));
        UploadImageRequest.ImageInfo info =
                UploadImageRequest.ImageInfo.newBuilder().setLaptopId(laptopId).setImageType(imageType).build();
        UploadImageRequest request = UploadImageRequest.newBuilder().setInfo(info).build();

        try {
            requestObserver.onNext(request);
            logger.info("sent image info:\n" + info);

            byte[] bytes = new byte[1024];
            int count;
            while ((count = fis.read(bytes)) != -1) {
                if (finishLatch.getCount() == 0) {
                    return;
                }

                request = UploadImageRequest.newBuilder()
                        .setChunkData(ByteString.copyFrom(bytes, 0, count))
                        .build();
                requestObserver.onNext(request);
                logger.info("sent image chunk with size: " + count);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "unexpected error: " + e.getMessage());
            requestObserver.onError(e);
            return;
        }

        requestObserver.onCompleted();

        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            logger.warning("request cannot finish within 1 minute");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LaptopClient client = new LaptopClient("localhost", 8080);
        Generator generator = new Generator();

        try {
//            // Test create and search laptops
//            createAndSearchLaptops(client, generator);

            // Test upload laptop image
            Laptop laptop = generator.newLaptop();
            client.createLaptop(laptop);
            client.uploadImage(laptop.getId(), "tmp/laptop.jpg");
        } finally {
            client.shutdown();
        }
    }

    private static void createAndSearchLaptops(LaptopClient client, Generator generator) {
        for (int i = 0; i < 10; i++) {
            Laptop laptop = generator.newLaptop();
            client.createLaptop(laptop);
        }

        Memory minRam = Memory.newBuilder()
                .setValue(8)
                .setUnit(Memory.Unit.GIGABYTE)
                .build();

        Filter filter = Filter.newBuilder()
                .setMaxPriceUsd(3000)
                .setMinCpuCores(4)
                .setMinCpuGhz(2.5)
                .setMinRam(minRam)
                .build();

        client.searchLaptop(filter);
    }
}
