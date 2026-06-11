package at.lukaswolf.sensor.config;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SensorConfig {

    public final String BROKER_URL = "tcp://10.190.29.128:1883";
    public final String CLIENT_ID = "raspberry-sensor-01";
    public final String TOPIC = "sensor/imu";
    public final int QOS = 1;

    public final int I2C_BUS = 1;
    public final int AG_ADDR = 0x6B;
    public final long INTERVAL_MS = 1000L;
}
