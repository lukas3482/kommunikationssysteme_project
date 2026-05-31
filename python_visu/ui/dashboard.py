from nicegui import ui

import config
from data.mqtt_client import MqttClient
from data.store import SensorStore
from ui.sensor_charts import SensorCharts
from ui.status_bar import StatusBar

_SRC = {
    "accel": "accelerometer",
    "gyro":  "gyroscope",
    "mag":   "magnetometer",
}


def register(store: SensorStore, mqtt_client: MqttClient) -> None:

    @ui.page("/")
    def _page() -> None:
        ui.page_title("IMU Dashboard")

        with ui.column().classes("w-full max-w-5xl mx-auto p-4 gap-4"):

            ui.label("IMU Dashboard").classes("text-xl font-bold")

            bar = StatusBar(config.BROKER, config.PORT, config.TOPIC)

            val_labels: dict[str, dict[str, ui.label]] = {}

            def _val_card(key: str, title: str, unit: str) -> None:
                with ui.card().classes("flex-1 min-w-44"):
                    ui.label(f"{title} [{unit}]").classes("text-sm font-semibold")
                    val_labels[key] = {}
                    for ax in "xyz":
                        with ui.row().classes("items-baseline gap-2"):
                            ui.label(ax.upper() + ":").classes("text-xs w-4")
                            val_labels[key][ax] = ui.label("—").classes("font-mono text-sm")

            with ui.row().classes("w-full gap-3 flex-wrap"):
                _val_card("accel", "Accelerometer", "g")
                _val_card("gyro",  "Gyroscope",     "°/s")
                _val_card("mag",   "Magnetometer",  "gauss")

            charts = SensorCharts()

        def _refresh() -> None:
            snap = store.snapshot()
            bar.refresh(mqtt_client.connected, store.count)

            if snap is None:
                return

            for key, src in _SRC.items():
                for ax in "xyz":
                    val_labels[key][ax].text = f"{snap['latest'][src][ax]:+.4f}"

            charts.refresh(snap)

        ui.timer(0.5, _refresh)
