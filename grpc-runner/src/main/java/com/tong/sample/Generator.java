package com.tong.sample;

import com.google.protobuf.Timestamp;
import com.tong.proto.*;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

public class Generator {
    public static void main(String[] args) {
        Generator generator = new Generator();
        Laptop laptop = generator.newLaptop();
        System.out.println(laptop);
    }

    private final Random RAND = new Random();

    public Laptop newLaptop() {
        String brand = randomLaptopBrand();
        String name = randomLaptopName(brand);

        double weightKg = randomDouble(1.0, 3.0);
        double priceUsd = randomDouble(1500, 3500);

        int releaseYear = randomInt(2018, 2022);

        return Laptop.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setBrand(brand)
                .setName(name)
                .setCpu(newCpu())
                .setRam(newRam())
                .addGpu(newGpu())
                .addStorage(newSsd())
                .addStorage(newHdd())
                .setKeyboard(newKeyboard())
                .setScreen(newScreen())
                .setWeightKg(weightKg)
                .setPriceUsd(priceUsd)
                .setReleaseYear(releaseYear)
                .setUpdatedAt(timestampNow())
                .build();
    }

    private Timestamp timestampNow() {
        Instant now = Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();
    }

    private String randomLaptopName(String brand) {
        switch (brand) {
            case "Apple":
                return randomStringFromSet("MacBook Air", "MacBook Pro");
            case "Dell":
                return randomStringFromSet("Latitude", "Vostro", "XPS", "AlienWare");
            default:
                return randomStringFromSet("ThinkPad X1", "ThinkPad P1", "ThinkPad P53");
        }
    }

    private String randomLaptopBrand() {
        return randomStringFromSet("Apple", "Dell", "Lenovo");
    }

    private Keyboard newKeyboard() {
        return Keyboard.newBuilder()
                .setLayout(randomKeyboardLayout())
                .setBacklit(RAND.nextBoolean())
                .build();
    }

    private Cpu newCpu() {
        String brand = randomCpuBrand();
        String name = randomCpuName(brand);

        int numberCores = randomInt(2, 8);
        int numberThreads = randomInt(numberCores, 12);

        double minGhz = randomDouble(2.0, 3.5);
        double maxGhz = randomDouble(minGhz, 5.0);

        return Cpu.newBuilder()
                .setBrand(brand)
                .setName(name)
                .setNumberCores(numberCores)
                .setNumberThreads(numberThreads)
                .setMinGhz(minGhz)
                .setMaxGhz(maxGhz)
                .build();
    }

    private Gpu newGpu() {
        String brand = randomGpuBrand();
        String name = randomGpuName(brand);

        double minGhz = randomDouble(1.0, 1.5);
        double maxGhz = randomDouble(minGhz, 2.0);

        Memory memory = Memory.newBuilder()
                .setValue(randomInt(2, 6))
                .setUnit(Memory.Unit.GIGABYTE)
                .build();

        return Gpu.newBuilder()
                .setBrand(brand)
                .setName(name)
                .setMinGhz(minGhz)
                .setMaxGhz(maxGhz)
                .setMemory(memory)
                .build();
    }

    private Memory newRam() {
        return Memory.newBuilder()
                .setValue(randomInt(4, 64))
                .setUnit(Memory.Unit.GIGABYTE)
                .build();
    }

    private Storage newSsd() {
        Memory memory = Memory.newBuilder()
                .setValue(randomInt(128, 1024))
                .setUnit(Memory.Unit.GIGABYTE)
                .build();

        return Storage.newBuilder()
                .setDriver(Storage.Driver.SSD)
                .setMemory(memory)
                .build();
    }

    private Storage newHdd() {
        Memory memory = Memory.newBuilder()
                .setValue(randomInt(1, 6))
                .setUnit(Memory.Unit.TERABYTE)
                .build();

        return Storage.newBuilder()
                .setDriver(Storage.Driver.HDD)
                .setMemory(memory)
                .build();
    }

    private Screen newScreen() {
        int height = randomInt(1080, 4320);
        int width = height * 16 / 9;

        Screen.Resolution resolution = Screen.Resolution.newBuilder()
                .setWidth(width)
                .setHeight(height)
                .build();

        return Screen.newBuilder()
                .setSizeInch(randomFloat(13, 17))
                .setResolution(resolution)
                .setPanel(randomScreenPanel())
                .setMultitouch(RAND.nextBoolean())
                .build();
    }

    private Screen.Panel randomScreenPanel() {
        if (RAND.nextBoolean()) {
            return Screen.Panel.IPS;
        }
        return Screen.Panel.OLED;
    }

    private float randomFloat(int min, int max) {
        return min + (max - min) * RAND.nextFloat();
    }

    private String randomGpuName(String brand) {
        if ("NVIDIA".equals(brand)) {
            return randomStringFromSet(
                    "RTX 2060",
                    "RTX 2070",
                    "GTX 1660-Ti",
                    "GTX 1070"
            );
        }

        return randomStringFromSet(
                "RX 590",
                "RX 580",
                "RX 5700-XT",
                "RX Vega-56"
        );
    }

    private String randomGpuBrand() {
        return randomStringFromSet("NVIDIA", "AMD");
    }

    private double randomDouble(double min, double max) {
        return min + (max - min) * RAND.nextDouble();
    }

    private int randomInt(int min, int max) {
        return min + RAND.nextInt(max - min + 1);
    }

    private String randomCpuName(String brand) {
        if ("Intel".equals(brand)) {
            return randomStringFromSet(
                    "Xeon E-2286M",
                    "Core i9-9980HK",
                    "Core i7-9750H",
                    "Core i5-9400F",
                    "Core i3-1005G1");
        }

        return randomStringFromSet(
                "Ryzen 7 PRO 2700U",
                "Ryzen 5 PRO 3500U",
                "Ryzen 3 PRO 3200GE"
        );
    }

    private String randomCpuBrand() {
        return randomStringFromSet("Intel", "AMD");
    }

    private String randomStringFromSet(String... a) {
        int n = a.length;
        if (n == 0) {
            return "";
        }
        return a[RAND.nextInt(n)];
    }

    private Keyboard.Layout randomKeyboardLayout() {
        switch (RAND.nextInt(3)) {
            case 1:
                return Keyboard.Layout.QWERTY;
            case 2:
                return Keyboard.Layout.QWERTZ;
            default:
                return Keyboard.Layout.AZERTY;
        }

    }
}
