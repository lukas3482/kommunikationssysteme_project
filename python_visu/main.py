import config
from data.mqtt_client import MqttClient
from nicegui import ui
from ui.dashboard import register

mqtt_client = MqttClient(config.BROKER, config.PORT, config.TOPIC, config.HISTORY)

register(mqtt_client)

ui.run(title="IMU Sensor", port=8080, reload=False, dark=False)
