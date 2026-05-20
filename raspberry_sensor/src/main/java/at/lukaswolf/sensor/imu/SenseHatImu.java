package at.lukaswolf.sensor.imu;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;

/**
 * IMU-Treiber für den Waveshare Sense HAT (C).
 *
 * Chips:
 *   Accel/Gyro  – QMI8658  (I2C 0x6B)
 *   Magnetometer – AK09918  (I2C 0x0C)
 */
public class SenseHatImu implements AutoCloseable {

    // ── QMI8658 Register ──────────────────────────────────────────────────────
    private static final int QMI8658_WHO_AM_I = 0x00;  // erwartet: 0x05
    private static final int QMI8658_CTRL1    = 0x02;  // I2C / SPI Modus
    private static final int QMI8658_CTRL2    = 0x03;  // Accelerometer config
    private static final int QMI8658_CTRL3    = 0x04;  // Gyroscope config
    private static final int QMI8658_CTRL5    = 0x06;  // Datenverarbeitung
    private static final int QMI8658_CTRL7    = 0x08;  // Sensor enable
    private static final int QMI8658_AX_L     = 0x35;  // Accel X Low (Block 12 Bytes: Ax..Gz)

    // QMI8658 CTRL7 Bits
    private static final int CTRL7_ACC_EN = 0x01;
    private static final int CTRL7_GYR_EN = 0x02;

    // ── AK09918 Register ──────────────────────────────────────────────────────
    private static final int AK09918_WIA2  = 0x01;  // Device ID, erwartet: 0x0C
    private static final int AK09918_ST1   = 0x10;  // Data Status 1
    private static final int AK09918_HXL   = 0x11;  // Mag X Low (Block 8 Bytes: HXL..ST2)
    private static final int AK09918_CNTL2 = 0x31;  // Betriebsmodus
    private static final int AK09918_CNTL3 = 0x32;  // Soft Reset
    private static final int AK09918_DRDY  = 0x01;  // Data Ready Bit in ST1
    private static final int AK09918_HOFL  = 0x08;  // Overflow Bit in ST2

    // ── Sensitivitäten ────────────────────────────────────────────────────────
    private static final double ACCEL_SENS = 1.0 / 4096.0;  // g/LSB     (QMI8658 ±2g)
    private static final double GYRO_SENS  = 1.0 / 64.0;    // °/s / LSB (QMI8658 ±512dps)
    private static final double MAG_SENS   = 0.15;           // µT / LSB  (AK09918, fest)

    private final Context pi4j;
    private final I2C qmi;  // QMI8658
    private final I2C ak;   // AK09918
    private final int[] gyroOffset = {0, 0, 0};

    public SenseHatImu(int i2cBus, int qmiAddr, int akAddr) {
        pi4j = Pi4J.newAutoContext();
        System.out.printf("I2C Bus %d – QMI8658=0x%02X  AK09918=0x%02X%n", i2cBus, qmiAddr, akAddr);

        I2CConfig qmiCfg = I2C.newConfigBuilder(pi4j).id("QMI8658").bus(i2cBus).device(qmiAddr).build();
        I2CConfig akCfg  = I2C.newConfigBuilder(pi4j).id("AK09918").bus(i2cBus).device(akAddr).build();

        qmi = pi4j.create(qmiCfg);
        ak  = pi4j.create(akCfg);
    }

    public void initialize() throws InterruptedException {
        initQmi8658();
        initAk09918();
    }

    // ── QMI8658 ───────────────────────────────────────────────────────────────

    private void initQmi8658() throws InterruptedException {
        int id = qmi.readRegister(QMI8658_WHO_AM_I);
        if (id != 0x05) {
            throw new IllegalStateException("QMI8658 nicht gefunden – WHO_AM_I=0x" + Integer.toHexString(id));
        }
        System.out.printf("QMI8658 OK (WHO_AM_I=0x%02X, Rev=0x%02X)%n", id, qmi.readRegister(0x01));

        qmi.writeRegister(QMI8658_CTRL1, (byte) 0x60);                           // I2C Modus
        qmi.writeRegister(QMI8658_CTRL2, (byte) 0x03);                           // Accel: ±2g, 1000 Hz
        qmi.writeRegister(QMI8658_CTRL3, (byte) 0x54);                           // Gyro: ±512dps, 500 Hz
        qmi.writeRegister(QMI8658_CTRL5, (byte) 0x00);                           // kein Low-Pass Filter
        qmi.writeRegister(QMI8658_CTRL7, (byte) (CTRL7_ACC_EN | CTRL7_GYR_EN | 0x80)); // Accel + Gyro an

        Thread.sleep(50);
        calibrateGyro();
    }

    /** Liest 32 Gyro-Samples und berechnet den Ruhewert-Offset (wie IMU.py QMI8658_GyroOffset). */
    private void calibrateGyro() throws InterruptedException {
        System.out.println("Gyro-Kalibrierung (32 Samples)...");
        long sx = 0, sy = 0, sz = 0;
        for (int i = 0; i < 32; i++) {
            byte[] raw = readQmiBlock();
            sx += toShort(raw, 6);
            sy += toShort(raw, 8);
            sz += toShort(raw, 10);
            Thread.sleep(10);
        }
        gyroOffset[0] = (int) (sx >> 5);
        gyroOffset[1] = (int) (sy >> 5);
        gyroOffset[2] = (int) (sz >> 5);
        System.out.printf("Gyro-Offset: X=%d  Y=%d  Z=%d%n", gyroOffset[0], gyroOffset[1], gyroOffset[2]);
    }

    /** Liest 12 Bytes Accel+Gyro-Rohdaten (Ax_L … Gz_H) per Block-Read. */
    private byte[] readQmiBlock() {
        byte[] buf = new byte[12];
        qmi.readRegister(QMI8658_AX_L, buf, 0, 12);
        return buf;
    }

    // ── AK09918 ───────────────────────────────────────────────────────────────

    private void initAk09918() throws InterruptedException {
        int id = ak.readRegister(AK09918_WIA2);
        if (id != 0x0C) {
            throw new IllegalStateException("AK09918 nicht gefunden – WIA2=0x" + Integer.toHexString(id));
        }
        System.out.printf("AK09918 OK (WIA2=0x%02X)%n", id);

        ak.writeRegister(AK09918_CNTL3, (byte) 0x01);  // Soft Reset
        Thread.sleep(10);
        ak.writeRegister(AK09918_CNTL2, (byte) 0x04);  // Continuous 20 Hz
    }

    /** Wartet auf Data-Ready (ST1.DRDY) und liest dann 8 Bytes Mag-Daten (wie IMU.py AK09918_MagRead). */
    private double[] readAk09918() throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            Thread.sleep(10);
            if ((ak.readRegister(AK09918_ST1) & AK09918_DRDY) != 0) break;
        }

        // 8 Bytes: HXL HXH HYL HYH HZL HZH TMPS ST2
        byte[] buf = new byte[8];
        ak.readRegister(AK09918_HXL, buf, 0, 8);

        if ((buf[7] & AK09918_HOFL) != 0) {
            System.err.println("AK09918: Magnetometer-Overflow!");
        }

        return new double[]{
            toShort(buf, 0) * MAG_SENS,
            toShort(buf, 2) * MAG_SENS,
            toShort(buf, 4) * MAG_SENS
        };
    }

    // ── Public read ───────────────────────────────────────────────────────────

    public ImuData read() throws InterruptedException {
        byte[] raw = readQmiBlock();

        double ax = toShort(raw, 0) * ACCEL_SENS;
        double ay = toShort(raw, 2) * ACCEL_SENS;
        double az = toShort(raw, 4) * ACCEL_SENS;

        double gx = (toShort(raw, 6)  - gyroOffset[0]) * GYRO_SENS;
        double gy = (toShort(raw, 8)  - gyroOffset[1]) * GYRO_SENS;
        double gz = (toShort(raw, 10) - gyroOffset[2]) * GYRO_SENS;

        double[] m = readAk09918();

        return new ImuData(ax, ay, az, gx, gy, gz, m[0], m[1], m[2]);
    }

    // ── Hilfsmethode ──────────────────────────────────────────────────────────

    /** Little-Endian 16-Bit signed aus zwei Bytes. */
    private short toShort(byte[] buf, int offset) {
        return (short) (((buf[offset + 1] & 0xFF) << 8) | (buf[offset] & 0xFF));
    }

    @Override
    public void close() {
        pi4j.shutdown();
    }
}
