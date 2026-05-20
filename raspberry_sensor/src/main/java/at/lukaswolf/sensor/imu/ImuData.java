package at.lukaswolf.sensor.imu;

public record ImuData(
        double accelX, double accelY, double accelZ,   // g
        double gyroX,  double gyroY,  double gyroZ,    // °/s
        double magX,   double magY,   double magZ      // Gauss
) {
    public String toJson() {
        return String.format(
            """
            {
              "timestamp": %d,
              "accelerometer": {"x": %.4f, "y": %.4f, "z": %.4f, "unit": "g"},
              "gyroscope":     {"x": %.4f, "y": %.4f, "z": %.4f, "unit": "deg/s"},
              "magnetometer":  {"x": %.4f, "y": %.4f, "z": %.4f, "unit": "gauss"}
            }""",
            System.currentTimeMillis(),
            accelX, accelY, accelZ,
            gyroX,  gyroY,  gyroZ,
            magX,   magY,   magZ
        );
    }

    @Override
    public String toString() {
        return String.format(
            "Accel[%.3f, %.3f, %.3f]g  Gyro[%.2f, %.2f, %.2f]°/s  Mag[%.3f, %.3f, %.3f]G",
            accelX, accelY, accelZ, gyroX, gyroY, gyroZ, magX, magY, magZ
        );
    }
}
