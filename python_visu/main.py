import asyncio

import config
from data.mqtt_client import MqttClient
from data.store import SensorStore
from nicegui import app, ui
from ui.dashboard import register


store       = SensorStore(history=config.HISTORY)
mqtt_client = MqttClient(config.BROKER, config.PORT, config.TOPIC, store)

register(store, mqtt_client)

ui.run(title="IMU Sensor", port=8080, reload=False, dark=False)
