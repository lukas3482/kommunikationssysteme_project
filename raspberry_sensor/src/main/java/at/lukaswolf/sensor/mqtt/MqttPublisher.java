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
    private final int qos;

    public MqttPublisher(SensorConfig config) throws MqttException {
        this.qos = config.getQos();

        options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        options.setKeepAliveInterval(30);
        options.setAutomaticReconnect(true);

        String username = config.getUsername();
        if (username != null && !username.isBlank()) {
            options.setUserName(username);
            options.setPassword(config.getPassword().toCharArray());
        }

        client = new MqttClient(config.getBrokerUrl(), config.getClientId(), new MemoryPersistence());
    }

    public void connect() throws MqttException {
        client.connect(options);
        System.out.println("Verbunden mit MQTT Broker: " + client.getServerURI());
    }

    public void publish(String topic, String payload) throws MqttException {
        if (!client.isConnected()) {
            System.err.println("MQTT nicht verbunden – Nachricht wird verworfen.");
            return;
        }
        MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
        message.setQos(qos);
        client.publish(topic, message);
    }

    @Override
    public void close() throws MqttException {
        if (client.isConnected()) {
            client.disconnect();
        }
        client.close();
    }
}
