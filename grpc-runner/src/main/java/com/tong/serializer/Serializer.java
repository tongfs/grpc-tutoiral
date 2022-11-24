package com.tong.serializer;

import com.google.protobuf.util.JsonFormat;
import com.tong.proto.Laptop;
import com.tong.sample.Generator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Serializer {
    public static void main(String[] args) throws IOException {
        String jsonFile = "laptop.txt";
        Laptop laptop = new Generator().newLaptop();
        new Serializer().writeJsonFile(laptop, jsonFile);
    }

    /**
     * 将 Laptop 对象序列化为二进制文件
     * 可以用其他语言尝试读取该二进制文件，经反序列化后得到的 JSON 文件和 writeJsonFile 方法得到的文件完全一致
     * @see Serializer#writeJsonFile(Laptop, String)
     */
    public void writeBinaryFile(Laptop laptop, String filename) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(filename);
        laptop.writeTo(outputStream);
        outputStream.close();
    }

    /**
     * 将二进制文件反序列化为 Laptop 对象
     */
    public Laptop readBinaryFile(String filename) throws IOException {
        FileInputStream inputStream = new FileInputStream(filename);
        Laptop laptop = Laptop.parseFrom(inputStream);
        inputStream.close();
        return laptop;
    }

    /**
     * 将 Laptop 对象写入 JSON 文件
     */
    public void writeJsonFile(Laptop laptop, String filename) throws IOException {
        JsonFormat.Printer printer = JsonFormat.printer()
                .includingDefaultValueFields()
                .preservingProtoFieldNames();

        String jsonString = printer.print(laptop);

        FileOutputStream outputStream = new FileOutputStream(filename);
        outputStream.write(jsonString.getBytes());
        outputStream.close();
    }
}
