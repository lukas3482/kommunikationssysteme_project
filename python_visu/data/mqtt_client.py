import json

import paho.mqtt.client as mqtt

from data.store import SensorStore


class MqttClient:
    def __init__(self, broker: str, port: int, topic: str, store: SensorStore) -> None:
        self._topic     = topic
        self._store     = store
        self._connected = False

        self._client = mqtt.Client()
        self._client.on_connect    = self._on_connect
        self._client.on_disconnect = self._on_disconnect
        self._client.on_message    = self._on_message

        try:
            self._client.connect(broker, port, keepalive=60)
            self._client.loop_start()
        except Exception as exc:
            print(f"MQTT connect error: {exc}")

    def _on_connect(self, client, userdata, flags, rc: int) -> None:
        self._connected = rc == 0
        if rc == 0:
            client.subscribe(self._topic)
        else:
            print(f"MQTT refused, rc={rc}")

    def _on_disconnect(self, client, userdata, rc: int) -> None:
        self._connected = False

    def _on_message(self, client, userdata, msg) -> None:
        try:
            self._store.push(json.loads(msg.payload))
        except Exception as exc:
            print(f"MQTT parse error: {exc}")

    @property
    def connected(self) -> bool:
        return self._connected
