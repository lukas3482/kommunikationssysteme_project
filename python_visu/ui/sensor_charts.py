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
        "backgroundColor": "#0f172a",
        "title": {
            "text": title,
            "textStyle": {"color": "#94a3b8", "fontSize": 13, "fontWeight": "normal"},
            "left": "10",
            "top": "6",
        },
        "tooltip": {
            "trigger": "axis",
            "backgroundColor": "#1e293b",
            "borderColor": "#334155",
            "textStyle": {"color": "#e2e8f0"},
        },
        "legend": {
            "data": ["X", "Y", "Z"],
            "bottom": 4,
            "textStyle": {"color": "#94a3b8"},
            "inactiveColor": "#475569",
        },
        "grid": {"top": "16%", "bottom": "18%", "left": "9%", "right": "3%"},
        "xAxis": {
            "type": "category",
            "data": [],
            "boundaryGap": False,
            "axisLine": {"lineStyle": {"color": "#334155"}},
            "axisLabel": {"color": "#475569", "rotate": 30, "fontSize": 9},
            "splitLine": {"show": False},
        },
        "yAxis": {
            "type": "value",
            "name": unit,
            "nameTextStyle": {"color": "#475569", "fontSize": 10},
            "axisLine": {"show": True, "lineStyle": {"color": "#334155"}},
            "splitLine": {"lineStyle": {"color": "#1e293b", "type": "dashed"}},
            "axisLabel": {"color": "#475569", "fontSize": 10},
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
                "areaStyle": {"color": _AXIS_COLORS[ax], "opacity": 0.05},
            }
            for ax in "xyz"
        ],
    }


class SensorCharts:
    """Three dark ECharts — one per sensor — showing X/Y/Z time series."""

    def __init__(self) -> None:
        self._charts: dict[str, ui.echart] = {}
        for key, title, unit in _SENSORS:
            with ui.card().classes(
                "w-full bg-slate-900 border border-slate-700 shadow-xl p-0 overflow-hidden"
            ):
                self._charts[key] = (
                    ui.echart(_build_opts(title, unit))
                    .classes("w-full h-52")
                )

    def refresh(self, snap: dict) -> None:
        ts = snap["ts"]
        for key, _, _ in _SENSORS:
            self._charts[key].run_chart_method("setOption", {
                "xAxis": {"data": ts},
                "series": [{"data": snap["series"][key][ax]} for ax in "xyz"],
            })
