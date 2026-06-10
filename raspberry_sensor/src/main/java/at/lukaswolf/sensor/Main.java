package at.lukaswolf.sensor;

import at.lukaswolf.sensor.config.SensorConfig;
import at.lukaswolf.sensor.imu.ImuData;
import at.lukaswolf.sensor.imu.SenseHatImu;
import at.lukaswolf.sensor.mqtt.MqttPublisher;

public class Main {

    public static void main(String[] args) throws Exception {
        SensorConfig config = SensorConfig.load();

        try (SenseHatImu imu = new SenseHatImu(config.getI2cBus(), config.getAgAddr(), config.getMagAddr());
             MqttPublisher mqtt = new MqttPublisher(config)) {

            imu.initialize();
            mqtt.connect();

            Thread mainThread = Thread.currentThread();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nSensor wird beendet...");
                mainThread.interrupt();
            }));

            System.out.printf("Sensor läuft – Topic: %s  Interval: %d ms%n",
                    config.getTopic(), config.getIntervalMs());

            while (!Thread.currentThread().isInterrupted()) {
                ImuData data = imu.read();
                mqtt.publish(config.getTopic(), data.toJson());
                System.out.println(data);
                Thread.sleep(config.getIntervalMs());
            }
        }

        System.out.println("Sensor beendet.");
    }
}
