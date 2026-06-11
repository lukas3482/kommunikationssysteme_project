package at.lukaswolf.sensor.imu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImuDataTest {

    private static final ImuData SAMPLE = new ImuData(
            1.0, -2.0, 9.81,
            0.5, -0.5, 0.0
    );

    @Test
    void getters_returnConstructedValues() {
        assertEquals(1.0,  SAMPLE.getAccelX(), 1e-9);
        assertEquals(-2.0, SAMPLE.getAccelY(), 1e-9);
        assertEquals(9.81, SAMPLE.getAccelZ(), 1e-9);
        assertEquals(0.5,  SAMPLE.getGyroX(),  1e-9);
        assertEquals(-0.5, SAMPLE.getGyroY(),  1e-9);
        assertEquals(0.0,  SAMPLE.getGyroZ(),  1e-9);
    }

    @Test
    void toJson_containsTimestampKey() {
        assertTrue(SAMPLE.toJson().contains("\"timestamp\""));
    }

    @Test
    void toJson_containsAllSensorGroups() {
        String json = SAMPLE.toJson();
        assertTrue(json.contains("\"accelerometer\""));
        assertTrue(json.contains("\"gyroscope\""));
    }

    @Test
    void toJson_valuesFormattedToFourDecimalPlaces() {
        String json = SAMPLE.toJson();
        assertTrue(json.contains("1.0000"), "accelX muss auf 4 Dezimalstellen formatiert sein");
        assertTrue(json.contains("9.8100"), "accelZ muss auf 4 Dezimalstellen formatiert sein");
    }

    @Test
    void toJson_containsUnitFields() {
        assertTrue(SAMPLE.toJson().contains("\"unit\""));
    }

    @Test
    void toString_containsAxisLabels() {
        String s = SAMPLE.toString();
        assertTrue(s.contains("Accel"));
        assertTrue(s.contains("Gyro"));
    }

    @Test
    void toString_containsNumericValues() {
        String s = SAMPLE.toString();
        assertTrue(s.contains("9.810") || s.contains("9.81"));
    }

    @Test
    void equality_sameValues_areEqual() {
        ImuData a = new ImuData(1, 2, 3, 4, 5, 6);
        ImuData b = new ImuData(1, 2, 3, 4, 5, 6);
        assertEquals(a, b);
    }

    @Test
    void equality_differentValues_areNotEqual() {
        ImuData a = new ImuData(1, 2, 3, 4, 5, 6);
        ImuData b = new ImuData(1, 2, 3, 4, 5, 0);
        assertNotEquals(a, b);
    }
}
