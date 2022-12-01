package com.tong.server;

import com.tong.proto.CreateLaptopRequest;
import com.tong.proto.CreateLaptopResponse;
import com.tong.proto.Laptop;
import com.tong.proto.LaptopServiceGrpc;
import com.tong.sample.Generator;
import com.tong.service.LaptopStoreService;
import com.tong.service.LaptopStoreServiceImpl;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * 测试 unary
 */
public class LaptopServerTest {

    private LaptopStoreService store;
    private LaptopServer server;
    private ManagedChannel channel;

    /**
     * 应该是用于释放 gRPC 连接资源
     */
    @Rule
    public GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Before
    public void setUp() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        InProcessServerBuilder serverBuilder = InProcessServerBuilder.forName(serverName).directExecutor();

        store = new LaptopStoreServiceImpl();
        server = new LaptopServer(serverBuilder, 0, store);
        server.start();

        channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void createLaptopWithAValidId() {
        Laptop laptop = new Generator().newLaptop();
        CreateLaptopRequest request = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        CreateLaptopResponse response = stub.createLaptop(request);

        assertNotNull(response);
        assertEquals(laptop.getId(), response.getId());

        Laptop found = store.find(response.getId());
        assertNotNull(found);
    }

    @Test
    public void createLaptopWithAnEmptyId() {
        Laptop laptop = new Generator().newLaptop().toBuilder().setId("").build();
        CreateLaptopRequest request = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        CreateLaptopResponse response = stub.createLaptop(request);

        assertNotNull(response);
        assertFalse(response.getId().isEmpty());

        Laptop found = store.find(response.getId());
        assertNotNull(found);
    }

    @Test(expected = StatusRuntimeException.class)
    public void createLaptopWithAnInvalidId() {
        Laptop laptop = new Generator().newLaptop().toBuilder().setId("invalid").build();
        CreateLaptopRequest request = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        CreateLaptopResponse response = stub.createLaptop(request);
    }

    @Test(expected = StatusRuntimeException.class)
    public void createLaptopWithAnAlreadyExistingId() throws Exception {
        Laptop laptop = new Generator().newLaptop().toBuilder().build();
        store.save(laptop);
        CreateLaptopRequest request = CreateLaptopRequest.newBuilder().setLaptop(laptop).build();

        LaptopServiceGrpc.LaptopServiceBlockingStub stub = LaptopServiceGrpc.newBlockingStub(channel);
        CreateLaptopResponse response = stub.createLaptop(request);
    }
}