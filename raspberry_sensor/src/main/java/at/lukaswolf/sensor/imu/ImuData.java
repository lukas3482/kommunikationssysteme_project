package at.lukaswolf.sensor.imu;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Locale;

@Getter
@EqualsAndHashCode
public class ImuData {

    private final double accelX, accelY, accelZ; // g
    private final double gyroX, gyroY, gyroZ; // °/s

    public ImuData(double accelX, double accelY, double accelZ,
                   double gyroX, double gyroY, double gyroZ) {
        this.accelX = accelX;
        this.accelY = accelY;
        this.accelZ = accelZ;
        this.gyroX = gyroX;
        this.gyroY = gyroY;
        this.gyroZ = gyroZ;
    }

    public String toJson() {
        return String.format(Locale.ROOT,
            """
            {
              "timestamp": %d,
              "accelerometer": {"x": %.4f, "y": %.4f, "z": %.4f, "unit": "g"},
              "gyroscope":     {"x": %.4f, "y": %.4f, "z": %.4f, "unit": "deg/s"}
            }""",
            System.currentTimeMillis(),
            accelX, accelY, accelZ,
            gyroX, gyroY, gyroZ
        );
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT,
            "Accel[%.3f, %.3f, %.3f]g  Gyro[%.2f, %.2f, %.2f]°/s",
            accelX, accelY, accelZ, gyroX, gyroY, gyroZ
        );
    }
}
