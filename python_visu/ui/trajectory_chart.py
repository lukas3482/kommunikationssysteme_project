import plotly.graph_objects as go
from nicegui import ui

_BG   = "#0f172a"
_GRID = "#1e293b"
_TEXT = "#94a3b8"


class TrajectoryChart:
    """Plotly 3D scatter showing the live XYZ trajectory of accel and mag."""

    def __init__(self) -> None:
        fig = go.Figure(
            data=[
                go.Scatter3d(
                    x=[], y=[], z=[],
                    mode="lines",
                    line=dict(color="#3b82f6", width=3),
                    name="Accel (g)",
                    opacity=0.9,
                ),
                go.Scatter3d(
                    x=[], y=[], z=[],
                    mode="lines",
                    line=dict(color="#f59e0b", width=2),
                    name="Mag (gauss)",
                    opacity=0.7,
                ),
            ],
            layout=go.Layout(
                scene=dict(
                    xaxis=dict(
                        title="X", color=_TEXT,
                        gridcolor=_GRID, backgroundcolor=_BG,
                    ),
                    yaxis=dict(
                        title="Y", color=_TEXT,
                        gridcolor=_GRID, backgroundcolor=_BG,
                    ),
                    zaxis=dict(
                        title="Z", color=_TEXT,
                        gridcolor=_GRID, backgroundcolor=_BG,
                    ),
                    bgcolor=_BG,
                ),
                paper_bgcolor=_BG,
                legend=dict(font=dict(color=_TEXT, size=11), bgcolor=_BG),
                margin=dict(l=0, r=0, t=10, b=0),
                font=dict(color=_TEXT),
            ),
        )
        with ui.card().classes(
            "flex-1 min-w-72 bg-slate-900 border border-slate-700 shadow-xl"
        ):
            ui.label("3D Trajektorie · Accel (blau) / Mag (gelb)").classes(
                "text-xs font-semibold text-slate-400 uppercase tracking-wider px-4 pt-3 pb-1"
            )
            self._plot = ui.plotly(fig).classes("w-full h-80")

    def refresh(self, snap: dict) -> None:
        sa = snap["series"]["accel"]
        sm = snap["series"]["mag"]
        self._plot.figure.data[0].x = sa["x"]
        self._plot.figure.data[0].y = sa["y"]
        self._plot.figure.data[0].z = sa["z"]
        self._plot.figure.data[1].x = sm["x"]
        self._plot.figure.data[1].y = sm["y"]
        self._plot.figure.data[1].z = sm["z"]
        self._plot.update()
