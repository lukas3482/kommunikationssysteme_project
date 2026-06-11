package at.lukaswolf.sensor.mqtt;

import at.lukaswolf.sensor.config.SensorConfig;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;

public class MqttPublisher implements AutoCloseable {

    private final MqttClient client;
    private final MqttConnectOptions options;

    public MqttPublisher() throws MqttException {
        this.options = new MqttConnectOptions();
        this.options.setCleanSession(true);
        this.options.setConnectionTimeout(10);
        this.options.setKeepAliveInterval(30);
        this.options.setAutomaticReconnect(true);

        this.client = new MqttClient(SensorConfig.BROKER_URL, SensorConfig.CLIENT_ID, new MemoryPersistence());
    }

    public void connect() throws MqttException {
        this.client.connect(options);
        System.out.println("Verbunden zu MQTT: " + this.client.getServerURI());
    }

    public void publish(String topic, String payload) throws MqttException {
        if (!this.client.isConnected()) {
            System.out.println("MQTT nicht verbunden...");
            return;
        }
        MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
        message.setQos(SensorConfig.QOS);
        this.client.publish(topic, message);
    }

    @Override
    public void close() throws MqttException {
        if (this.client.isConnected()) {
            this.client.disconnect();
        }
        this.client.close();
    }
}
