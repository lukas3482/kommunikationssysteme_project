from nicegui import ui

_AXIS_COLORS = {"x": "#ef4444", "y": "#22c55e", "z": "#3b82f6"}

_SENSORS = [
    ("accel", "Accelerometer", "g"),
    ("gyro",  "Gyroscope",     "°/s"),
    ("mag",   "Magnetometer",  "gauss"),
]


def _build_opts(title: str, unit: str) -> dict:
    return {
        "animation": False,
        "title": {"text": title, "left": "10", "top": "6"},
        "tooltip": {"trigger": "axis"},
        "legend": {"data": ["X", "Y", "Z"], "bottom": 4},
        "grid": {"top": "30%", "bottom": "18%", "left": "9%", "right": "3%"},
        "xAxis": {
            "type": "category",
            "data": [],
            "boundaryGap": False,
            "axisLabel": {"rotate": 30, "fontSize": 9},
            "splitLine": {"show": False},
        },
        "yAxis": {
            "type": "value",
            "name": unit,
        },
        "series": [
            {
                "name": ax.upper(),
                "type": "line",
                "data": [],
                "smooth": True,
                "symbol": "none",
                "lineStyle": {"width": 1.5, "color": _AXIS_COLORS[ax]},
                "itemStyle": {"color": _AXIS_COLORS[ax]},
            }
            for ax in "xyz"
        ],
    }


class SensorCharts:
    def __init__(self) -> None:
        self._charts: dict[str, ui.echart] = {}
        for key, title, unit in _SENSORS:
            with ui.card().classes("w-full"):
                self._charts[key] = ui.echart(_build_opts(title, unit)).classes("w-full h-52")

    def refresh(self, snap: dict) -> None:
        ts = snap["ts"]
        for key, _, _ in _SENSORS:
            self._charts[key].run_chart_method("setOption", {
                "xAxis": {"data": ts},
                "series": [{"data": snap["series"][key][ax]} for ax in "xyz"],
            })
