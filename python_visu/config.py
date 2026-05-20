import os

BROKER  = os.getenv("MQTT_BROKER", "localhost")
PORT    = int(os.getenv("MQTT_PORT", "1883"))
TOPIC   = "sensor/imu"
HISTORY = 150
