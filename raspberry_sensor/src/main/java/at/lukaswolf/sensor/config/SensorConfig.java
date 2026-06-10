package at.lukaswolf.sensor.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SensorConfig {

    private final Properties props;

    private SensorConfig(Properties props) {
        this.props = props;
    }

    public static SensorConfig load() throws IOException {
        Properties props = new Properties();

        try (InputStream in = SensorConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) props.load(in);
        }

        File external = new File("config.properties");
        if (external.exists()) {
            try (InputStream in = new FileInputStream(external)) {
                props.load(in);
            }
            System.out.println("Externe config.properties geladen: " + external.getAbsolutePath());
        }

        return new SensorConfig(props);
    }

    public String getBrokerUrl() {
        return props.getProperty("mqtt.broker", "tcp://localhost:1883");
    }
    public String getClientId() {
        return props.getProperty("mqtt.clientId", "raspberry-sensor");
    }
    public String getTopic() {
        return props.getProperty("mqtt.topic", "sensor/imu");
    }
    public int getQos() {
        return Integer.parseInt(props.getProperty("mqtt.qos", "1"));
    }
    public String getUsername(){
        return props.getProperty("mqtt.username");
    }
    public String getPassword() {
        return props.getProperty("mqtt.password", "");
    }
    public int getI2cBus() {
        return Integer.parseInt(props.getProperty("sensor.i2cBus", "1"));
    }
    public int getAgAddr() {
        return Integer.decode(props.getProperty("sensor.agAddr",  "0x6A"));
    }
    public int getMagAddr() {
        return Integer.decode(props.getProperty("sensor.magAddr", "0x1C"));
    }
    public long getIntervalMs() {
        return Long.parseLong(props.getProperty("sensor.intervalMs", "1000"));
    }
}
