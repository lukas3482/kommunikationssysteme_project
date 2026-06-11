import copy
import json
import threading
from collections import deque
from datetime import datetime

import paho.mqtt.client as mqtt


class MqttClient:
    def __init__(self, broker: str, port: int, topic: str, history: int = 150):
        self._topic     = topic
        self._connected = False
        self._lock      = threading.Lock()
        self._ts        = deque(maxlen=history)
        self._buf       = {
            "accel": {ax: deque(maxlen=history) for ax in "xyz"},
            "gyro":  {ax: deque(maxlen=history) for ax in "xyz"},
        }
        self._latest: dict = {}
        self._count  = 0

        self._client = mqtt.Client()
        self._client.on_connect    = self._on_connect
        self._client.on_disconnect = self._on_disconnect
        self._client.on_message    = self._on_message

        try:
            self._client.connect(broker, port, keepalive=60)
            self._client.loop_start()
        except Exception as exc:
            print(f"MQTT connect error: {exc}")

    def _on_connect(self, client, userdata, flags, rc: int):
        self._connected = rc == 0
        if rc == 0:
            client.subscribe(self._topic)
        else:
            print(f"MQTT refused, rc={rc}")

    def _on_disconnect(self, client, userdata, rc: int):
        self._connected = False

    def _on_message(self, client, userdata, msg):
        try:
            data = json.loads(msg.payload)
            with self._lock:
                self._ts.append(data.get("timestamp", 0))
                for ax in "xyz":
                    self._buf["accel"][ax].append(data["accelerometer"][ax])
                    self._buf["gyro"][ax].append(data["gyroscope"][ax])
                self._latest = data
                self._count += 1
        except Exception as exc:
            print(f"MQTT parse error: {exc}")

    def snapshot(self) -> dict | None:
        with self._lock:
            if not self._latest:
                return None
            return {
                "latest": copy.deepcopy(self._latest),
                "ts": [
                    datetime.fromtimestamp(t / 1000).strftime("%H:%M:%S")
                    for t in self._ts
                ],
                "series": {
                    grp: {ax: list(self._buf[grp][ax]) for ax in "xyz"}
                    for grp in ("accel", "gyro")
                },
                "count": self._count,
            }

    def connected(self) -> bool:
        return self._connected

    def count(self) -> int:
        return self._count
