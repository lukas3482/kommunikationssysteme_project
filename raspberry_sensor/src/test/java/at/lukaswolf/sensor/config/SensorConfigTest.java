package at.lukaswolf.sensor.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SensorConfigTest {

    @Test
    void brokerUrl_isNotNullOrBlank() {
        assertNotNull(SensorConfig.BROKER_URL);
        assertFalse(SensorConfig.BROKER_URL.isBlank());
    }

    @Test
    void brokerUrl_startsWithTcp() {
        assertTrue(SensorConfig.BROKER_URL.startsWith("tcp://") || SensorConfig.BROKER_URL.startsWith("ssl://"),
                "Broker-URL muss mit tcp:// oder ssl:// beginnen, war: " + SensorConfig.BROKER_URL);
    }

    @Test
    void topic_isNotNullOrBlank() {
        assertNotNull(SensorConfig.TOPIC);
        assertFalse(SensorConfig.TOPIC.isBlank());
    }

    @Test
    void intervalMs_isPositive() {
        assertTrue(SensorConfig.INTERVAL_MS > 0);
    }

    @Test
    void i2cBus_isNonNegative() {
        assertTrue(SensorConfig.I2C_BUS >= 0);
    }

    @Test
    void qos_isValidRange() {
        assertTrue(SensorConfig.QOS >= 0 && SensorConfig.QOS <= 2,
                "QoS muss 0, 1 oder 2 sein, war: " + SensorConfig.QOS);
    }

    @Test
    void clientId_isNotNullOrBlank() {
        assertNotNull(SensorConfig.CLIENT_ID);
        assertFalse(SensorConfig.CLIENT_ID.isBlank());
    }
}
