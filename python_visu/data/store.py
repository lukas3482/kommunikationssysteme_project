import copy
import threading
from collections import deque
from datetime import datetime


class SensorStore:
    def __init__(self, history: int = 150):
        self._lock  = threading.Lock()
        self._ts    = deque(maxlen=history)
        self._buf   = {
            "accel": {ax: deque(maxlen=history) for ax in "xyz"},
            "gyro":  {ax: deque(maxlen=history) for ax in "xyz"},
        }
        self._latest: dict = {}
        self._count  = 0

    def push(self, data: dict):
        with self._lock:
            self._ts.append(data.get("timestamp", 0))
            for ax in "xyz":
                self._buf["accel"][ax].append(data["accelerometer"][ax])
                self._buf["gyro"][ax].append(data["gyroscope"][ax])
            self._latest = data
            self._count += 1

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

    @property
    def count(self) -> int:
        return self._count
