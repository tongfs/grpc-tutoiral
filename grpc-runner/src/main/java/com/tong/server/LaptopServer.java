package com.tong.server;

import com.tong.service.LaptopService;
import com.tong.service.LaptopStoreService;
import com.tong.service.LaptopStoreServiceInMemory;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LaptopServer {

    private final Logger logger = Logger.getLogger(LaptopServer.class.getName());

    private final int PORT;
    private final Server server;

    public LaptopServer(int port, LaptopStoreService store) {
        this(ServerBuilder.forPort(port), port, store);
    }

    public LaptopServer(ServerBuilder serverBuilder, int port, LaptopStoreService store) {
        this.PORT = port;
        LaptopService laptopService = new LaptopService(store);
        server = serverBuilder.addService(laptopService).build();
    }

    public void start() throws IOException {
        server.start();
        logger.info("server started on port: " + PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("shutdown gRPC server because JVM shuts down");
            try {
                stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("server shutdown");
        }));
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        LaptopStoreService store = new LaptopStoreServiceInMemory();
        LaptopServer server = new LaptopServer(8080, store);
        server.start();
        server.blockUntilShutdown();
    }
}
