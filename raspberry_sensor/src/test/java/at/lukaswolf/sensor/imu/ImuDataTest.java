package at.lukaswolf.sensor.imu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImuDataTest {

    private static final ImuData SAMPLE = new ImuData(
            1.0, -2.0, 9.81,
            0.5, -0.5, 0.0,
            0.3,  0.1, -0.2
    );

    @Test
    void recordAccessors_returnConstructedValues() {
        assertEquals(1.0,   SAMPLE.accelX(), 1e-9);
        assertEquals(-2.0,  SAMPLE.accelY(), 1e-9);
        assertEquals(9.81,  SAMPLE.accelZ(), 1e-9);
        assertEquals(0.5,   SAMPLE.gyroX(),  1e-9);
        assertEquals(-0.5,  SAMPLE.gyroY(),  1e-9);
        assertEquals(0.0,   SAMPLE.gyroZ(),  1e-9);
        assertEquals(0.3,   SAMPLE.magX(),   1e-9);
        assertEquals(0.1,   SAMPLE.magY(),   1e-9);
        assertEquals(-0.2,  SAMPLE.magZ(),   1e-9);
    }

    @Test
    void toJson_containsTimestampKey() {
        String json = SAMPLE.toJson();
        assertTrue(json.contains("\"timestamp\""), "JSON muss timestamp enthalten");
    }

    @Test
    void toJson_containsAllSensorGroups() {
        String json = SAMPLE.toJson();
        assertTrue(json.contains("\"accelerometer\""));
        assertTrue(json.contains("\"gyroscope\""));
        assertTrue(json.contains("\"magnetometer\""));
    }

    @Test
    void toJson_valuesFormattedToFourDecimalPlaces() {
        String json = SAMPLE.toJson();
        // accelX = 1.0 → "1.0000"
        assertTrue(json.contains("1.0000"), "accelX muss auf 4 Dezimalstellen formatiert sein");
        // magZ = -0.2 → "-0.2000"
        assertTrue(json.contains("-0.2000"));
    }

    @Test
    void toJson_containsUnitFields() {
        String json = SAMPLE.toJson();
        assertTrue(json.contains("\"unit\""));
    }

    @Test
    void toString_containsAxisLabels() {
        String s = SAMPLE.toString();
        assertTrue(s.contains("Accel"));
        assertTrue(s.contains("Gyro"));
        assertTrue(s.contains("Mag"));
    }

    @Test
    void toString_containsNumericValues() {
        String s = SAMPLE.toString();
        assertTrue(s.contains("9.810") || s.contains("9.81"));
    }

    @Test
    void equality_sameValues_areEqual() {
        ImuData a = new ImuData(1, 2, 3, 4, 5, 6, 7, 8, 9);
        ImuData b = new ImuData(1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertEquals(a, b);
    }

    @Test
    void equality_differentValues_areNotEqual() {
        ImuData a = new ImuData(1, 2, 3, 4, 5, 6, 7, 8, 9);
        ImuData b = new ImuData(1, 2, 3, 4, 5, 6, 7, 8, 0);
        assertNotEquals(a, b);
    }
}
