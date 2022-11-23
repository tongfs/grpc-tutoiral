package com.tong.serializer;

import com.tong.proto.Laptop;
import com.tong.sample.Generator;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class SerializerTest {

    @Test
    public void testWriteAndWriteBinaryFile() throws IOException {
        String binaryFile = "laptop.bin";
        Laptop laptop = new Generator().newLaptop();

        Serializer serializer = new Serializer();
        serializer.writeBinaryFile(laptop, binaryFile);

        Laptop newLaptop = serializer.readBinaryFile(binaryFile);
        Assert.assertEquals(laptop, newLaptop);
    }
}