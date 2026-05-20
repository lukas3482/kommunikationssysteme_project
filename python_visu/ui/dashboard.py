from nicegui import ui

import config
from data.mqtt_client import MqttClient
from data.store import SensorStore
from ui.imu_scene import IMUScene
from ui.sensor_charts import SensorCharts
from ui.status_bar import StatusBar
from ui.trajectory_chart import TrajectoryChart

_SRC = {
    "accel": "accelerometer",
    "gyro":  "gyroscope",
    "mag":   "magnetometer",
}


def register(store: SensorStore, mqtt_client: MqttClient) -> None:

    @ui.page("/")
    def _page() -> None:
        ui.dark_mode().enable()
        ui.query("body").classes(add="bg-slate-950")
        ui.page_title("IMU Dashboard")

        with ui.column().classes("w-full max-w-6xl mx-auto p-4 gap-4"):

            # ── Header ──────────────────────────────────────────────────────
            with ui.row().classes("w-full items-center gap-3"):
                ui.icon("sensors").classes("text-blue-500 text-3xl")
                ui.label("IMU Dashboard").classes(
                    "text-2xl font-bold text-slate-100 tracking-tight"
                )

            # ── MQTT status ──────────────────────────────────────────────────
            bar = StatusBar(config.BROKER, config.PORT, config.TOPIC)

            # ── Current value cards ─────────────────────────────────────────
            val_labels: dict[str, dict[str, ui.label]] = {}

            def _val_card(key: str, title: str, unit: str) -> None:
                ax_colors = {"x": "text-red-400", "y": "text-green-400", "z": "text-blue-400"}
                with ui.card().classes(
                    "flex-1 min-w-44 bg-slate-800 border border-slate-700 shadow-lg"
                ):
                    ui.label(title).classes(
                        "text-xs font-semibold text-slate-400 uppercase tracking-wider"
                    )
                    ui.label(unit).classes("text-xs text-slate-600 mb-1")
                    val_labels[key] = {}
                    for ax, color in ax_colors.items():
                        with ui.row().classes("items-baseline gap-2"):
                            ui.label(ax.upper() + ":").classes(
                                "text-xs font-mono text-slate-500 w-4"
                            )
                            val_labels[key][ax] = ui.label("—").classes(
                                f"font-mono text-sm {color}"
                            )

            with ui.row().classes("w-full gap-3 flex-wrap"):
                _val_card("accel", "Accelerometer", "g")
                _val_card("gyro",  "Gyroscope",     "°/s")
                _val_card("mag",   "Magnetometer",  "gauss")

            # ── 3D views ─────────────────────────────────────────────────────
            with ui.row().classes("w-full gap-4 flex-wrap"):
                scene = IMUScene()
                traj  = TrajectoryChart()

            # ── Time-series charts ───────────────────────────────────────────
            charts = SensorCharts()

        # ── Fast timer: labels, status, 3D scene, ECharts (500 ms) ──────────
        def _refresh_fast() -> None:
            snap = store.snapshot()
            bar.refresh(mqtt_client.connected, store.count)

            if snap is None:
                return

            for key, src in _SRC.items():
                for ax in "xyz":
                    val_labels[key][ax].text = f"{snap['latest'][src][ax]:+.4f}"

            scene.refresh(snap)
            charts.refresh(snap)

        # ── Slow timer: Plotly 3D trajectory (2 s) ───────────────────────────
        def _refresh_slow() -> None:
            snap = store.snapshot()
            if snap is not None:
                traj.refresh(snap)

        ui.timer(0.5, _refresh_fast)
        ui.timer(2.0, _refresh_slow)
