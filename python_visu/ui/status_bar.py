from nicegui import ui


class StatusBar:
    def __init__(self, broker: str, port: int, topic: str):
        self._broker = broker
        self._port   = port
        self._topic  = topic
        with ui.row().classes("items-center gap-3"):
            self._dot   = ui.icon("circle", size="xs")
            self._label = ui.label()
            self._count = ui.label().classes("ml-auto font-mono text-sm")

    def refresh(self, connected: bool, count: int):
        self._dot.classes(
            remove="text-green-600 text-red-600",
            add="text-green-600" if connected else "text-red-600",
        )
        self._label.text = (
            f"Verbunden  ·  {self._broker}:{self._port}  ·  {self._topic}"
            if connected
            else f"Nicht verbunden  ·  {self._broker}:{self._port}"
        )
        self._count.text = f"{count:,} msgs"
