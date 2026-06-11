import asyncio

import config
from data.mqtt_client import MqttClient
from data.store import SensorStore
from nicegui import app, ui
from ui.dashboard import register


def _suppress_connection_reset(loop: asyncio.AbstractEventLoop, context: dict):
    exc = context.get("exception")
    if isinstance(exc, (ConnectionResetError, OSError)) and getattr(exc, "winerror", None) == 10054:
        return
    loop.default_exception_handler(context)


@app.on_startup
async def _setup():
    asyncio.get_running_loop().set_exception_handler(_suppress_connection_reset)


store       = SensorStore(history=config.HISTORY)
mqtt_client = MqttClient(config.BROKER, config.PORT, config.TOPIC, store)

register(store, mqtt_client)

ui.run(title="IMU Sensor", port=8080, reload=False, dark=False)
