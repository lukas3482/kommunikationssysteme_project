package at.lukaswolf.sensor.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SensorConfigTest {

    private SensorConfig load() throws IOException {
        return SensorConfig.load();
    }

    @Test
    void load_doesNotThrow() {
        assertDoesNotThrow(this::load);
    }

    @Test
    void brokerUrl_isNotNullOrBlank() throws IOException {
        String url = load().getBrokerUrl();
        assertNotNull(url);
        assertFalse(url.isBlank());
    }

    @Test
    void brokerUrl_startsWithTcp() throws IOException {
        // bundled config.properties sets tcp://… – verify protocol prefix
        String url = load().getBrokerUrl();
        assertTrue(url.startsWith("tcp://") || url.startsWith("ssl://"),
                "Broker-URL muss mit tcp:// oder ssl:// beginnen, war: " + url);
    }

    @Test
    void topic_isNotNullOrBlank() throws IOException {
        String topic = load().getTopic();
        assertNotNull(topic);
        assertFalse(topic.isBlank());
    }

    @Test
    void intervalMs_isPositive() throws IOException {
        assertTrue(load().getIntervalMs() > 0);
    }

    @Test
    void i2cBus_isNonNegative() throws IOException {
        assertTrue(load().getI2cBus() >= 0);
    }

    @Test
    void qos_isValidRange() throws IOException {
        int qos = load().getQos();
        assertTrue(qos >= 0 && qos <= 2, "QoS muss 0, 1 oder 2 sein, war: " + qos);
    }

    @Test
    void clientId_isNotNullOrBlank() throws IOException {
        String id = load().getClientId();
        assertNotNull(id);
        assertFalse(id.isBlank());
    }
}
