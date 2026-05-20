from nicegui import ui


class StatusBar:
    def __init__(self, broker: str, port: int, topic: str) -> None:
        self._broker = broker
        self._port   = port
        self._topic  = topic
        with ui.row().classes(
            "w-full items-center gap-3 px-4 py-2 bg-slate-800 rounded-xl border border-slate-700"
        ):
            self._dot   = ui.icon("circle", size="xs")
            self._label = ui.label().classes("text-sm text-slate-300")
            self._count = ui.label().classes("ml-auto text-xs text-slate-500 font-mono")

    def refresh(self, connected: bool, count: int) -> None:
        self._dot.classes(
            remove="text-green-400 text-red-400",
            add="text-green-400" if connected else "text-red-400",
        )
        self._label.text = (
            f"Verbunden  ·  {self._broker}:{self._port}  ·  {self._topic}"
            if connected
            else f"Nicht verbunden  ·  {self._broker}:{self._port}"
        )
        self._count.text = f"{count:,} msgs"
