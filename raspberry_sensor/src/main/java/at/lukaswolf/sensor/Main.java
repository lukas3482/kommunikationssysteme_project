package at.lukaswolf.sensor;

import at.lukaswolf.sensor.config.SensorConfig;
import at.lukaswolf.sensor.imu.ImuData;
import at.lukaswolf.sensor.imu.SenseHatImu;
import at.lukaswolf.sensor.mqtt.MqttPublisher;

public class Main {

    public static void main(String[] args) throws Exception {
        try (SenseHatImu imu = new SenseHatImu(SensorConfig.I2C_BUS, SensorConfig.AG_ADDR);
             MqttPublisher mqtt = new MqttPublisher()) {

            imu.initialize();
            mqtt.connect();

            Thread mainThread = Thread.currentThread();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nSensor wird beendet...");
                mainThread.interrupt();
            }));

            System.out.printf("Sensor läuft – Topic: %s  Interval: %d ms%n",
                    SensorConfig.TOPIC, SensorConfig.INTERVAL_MS);

            while (!Thread.currentThread().isInterrupted()) {
                ImuData data = imu.read();
                mqtt.publish(SensorConfig.TOPIC, data.toJson());
                System.out.println(data);
                Thread.sleep(SensorConfig.INTERVAL_MS);
            }
        }

        System.out.println("Sensor beendet.");
    }
}
