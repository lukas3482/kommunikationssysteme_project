package at.lukaswolf.sensor.imu;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;

/**
 * IMU-Treiber für den Waveshare Sense HAT (C).
 *
 * Chip: Accel/Gyro – QMI8658 (I2C 0x6B)
 */
public class SenseHatImu implements AutoCloseable {

    private static final int QMI8658_WHO_AM_I = 0x00;
    private static final int QMI8658_CTRL1 = 0x02; // I2C / SPI Modus
    private static final int QMI8658_CTRL2 = 0x03; // Accelerometer config
    private static final int QMI8658_CTRL3 = 0x04; // Gyroscope config
    private static final int QMI8658_CTRL5 = 0x06; // Datenverarbeitung
    private static final int QMI8658_CTRL7 = 0x08; // Sensor enable
    private static final int QMI8658_AX_L = 0x35; // Accel X Low (Block 12 Bytes: Ax..Gz)

    private static final int CTRL7_ACC_EN = 0x01;
    private static final int CTRL7_GYR_EN = 0x02;

    private static final double ACCEL_SENS = 1.0 / 4096.0;
    private static final double GYRO_SENS = 1.0 / 64.0;

    private final Context pi4j;
    private final I2C qmi;
    private final int[] gyroOffset = {0, 0, 0};

    public SenseHatImu(int i2cBus, int qmiAddr) {
        this.pi4j = Pi4J.newAutoContext();
        System.out.printf("I2C Bus %d – QMI8658=0x%02X%n", i2cBus, qmiAddr);

        I2CConfig qmiCfg = I2C.newConfigBuilder(this.pi4j).id("QMI8658").bus(i2cBus).device(qmiAddr).build();
        this.qmi = this.pi4j.create(qmiCfg);
    }

    public void initialize() throws InterruptedException {
        initQmi8658();
    }

    private void initQmi8658() throws InterruptedException {
        int id = this.qmi.readRegister(QMI8658_WHO_AM_I);
        if (id != 0x05) {
            throw new IllegalStateException("QMI8658 nicht gefunden WHO_AM_I=0x" + Integer.toHexString(id));
        }
        System.out.printf("QMI8658 gefunden (WHO_AM_I=0x%02X, Rev=0x%02X)%n", id, this.qmi.readRegister(0x01));

        this.qmi.writeRegister(QMI8658_CTRL1, (byte) 0x60); // I2C Modus
        this.qmi.writeRegister(QMI8658_CTRL2, (byte) 0x03); // Accel: ±2g, 1000 Hz
        this.qmi.writeRegister(QMI8658_CTRL3, (byte) 0x54); // Gyro: ±512dps, 500 Hz
        this.qmi.writeRegister(QMI8658_CTRL5, (byte) 0x00); // kein Low-Pass Filter
        this.qmi.writeRegister(QMI8658_CTRL7, (byte) (CTRL7_ACC_EN | CTRL7_GYR_EN | 0x80)); // Accel + Gyro an

        Thread.sleep(50);
        calibrateGyro();
    }


    private void calibrateGyro() throws InterruptedException {
        System.out.println("Gyro-Kalibrierung ...");
        long sx = 0, sy = 0, sz = 0;
        for (int i = 0; i < 32; i++) {
            byte[] raw = readQmiBlock();
            sx += toShort(raw, 6);
            sy += toShort(raw, 8);
            sz += toShort(raw, 10);
            Thread.sleep(10);
        }
        this.gyroOffset[0] = (int) (sx >> 5);
        this.gyroOffset[1] = (int) (sy >> 5);
        this.gyroOffset[2] = (int) (sz >> 5);
        System.out.printf("Gyro-Offset: X=%d  Y=%d  Z=%d%n", this.gyroOffset[0], this.gyroOffset[1], this.gyroOffset[2]);
    }

    private byte[] readQmiBlock() {
        byte[] buf = new byte[12];
        this.qmi.readRegister(QMI8658_AX_L, buf, 0, 12);
        return buf;
    }

    public ImuData read() throws InterruptedException {
        byte[] raw = readQmiBlock();

        double ax = toShort(raw, 0) * ACCEL_SENS;
        double ay = toShort(raw, 2) * ACCEL_SENS;
        double az = toShort(raw, 4) * ACCEL_SENS;

        double gx = (toShort(raw, 6) - this.gyroOffset[0]) * GYRO_SENS;
        double gy = (toShort(raw, 8) - this.gyroOffset[1]) * GYRO_SENS;
        double gz = (toShort(raw, 10) - this.gyroOffset[2]) * GYRO_SENS;

        return new ImuData(ax, ay, az, gx, gy, gz);
    }

    private short toShort(byte[] buf, int offset) {
        return (short) (((buf[offset + 1] & 0xFF) << 8) | (buf[offset] & 0xFF));
    }

    @Override
    public void close() {
        this.pi4j.shutdown();
    }
}
