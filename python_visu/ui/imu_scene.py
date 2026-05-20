import math

from nicegui import ui


class IMUScene:
    """3D PCB board that rotates to reflect accelerometer pitch/roll."""

    def __init__(self) -> None:
        with ui.card().classes(
            "flex-1 min-w-72 bg-slate-900 border border-slate-700 shadow-xl"
        ):
            with ui.row().classes("items-center gap-2 px-4 pt-3 pb-1"):
                ui.label("Live-Orientierung").classes(
                    "text-xs font-semibold text-slate-400 uppercase tracking-wider"
                )
                ui.label("· Accel pitch / roll").classes("text-xs text-slate-600")

            with ui.scene(
                width=440,
                height=320,
                grid=False,
                background_color="#0f172a",
            ) as self._scene:

                # ── Fixed world-frame axes ────────────────────────────────────
                self._scene.box(5.0, 0.03, 0.03).material("#ef4444", opacity=0.4).move(2.5, 0, 0)
                self._scene.box(0.03, 5.0, 0.03).material("#22c55e", opacity=0.4).move(0, 2.5, 0)
                self._scene.box(0.03, 0.03, 5.0).material("#3b82f6", opacity=0.4).move(0, 0, 2.5)

                # ── PCB group — every child rotates together ──────────────────
                with self._scene.group() as self._group:
                    # PCB body (green FR4)
                    self._scene.box(3.6, 0.08, 2.4).material("#14532d", opacity=0.97)
                    # PCB surface highlight (lighter green layer)
                    self._scene.box(3.55, 0.005, 2.35).material("#166534", opacity=0.6).move(0, 0.043, 0)

                    # IMU sensor chip (dark body)
                    self._scene.box(0.65, 0.13, 0.65).material("#18181b").move(0, 0.105, 0)
                    # Chip gold top markings
                    self._scene.box(0.42, 0.005, 0.42).material("#a16207", opacity=0.8).move(0, 0.173, 0)

                    # Gold solder pads along edges
                    for cx in (-1.4, -0.7, 0.0, 0.7, 1.4):
                        self._scene.box(0.10, 0.005, 0.18).material("#ca8a04").move(cx, 0.043,  1.12)
                        self._scene.box(0.10, 0.005, 0.18).material("#ca8a04").move(cx, 0.043, -1.12)
                    for cz in (-0.8, 0.0, 0.8):
                        self._scene.box(0.18, 0.005, 0.10).material("#ca8a04").move( 1.77, 0.043, cz)
                        self._scene.box(0.18, 0.005, 0.10).material("#ca8a04").move(-1.77, 0.043, cz)

                    # Board-local X axis indicator (red)
                    self._scene.box(2.2, 0.04, 0.05).material("#ef4444").move(1.1, 0.065, 0)
                    # Board-local Z axis indicator (blue)
                    self._scene.box(0.05, 0.04, 1.6).material("#3b82f6").move(0, 0.065, 0.8)

    def refresh(self, snap: dict) -> None:
        a = snap["latest"]["accelerometer"]
        ax, ay, az = a["x"], a["y"], a["z"]

        # Normalize — removes any sensor scale factor or DC offset
        mag = math.sqrt(ax * ax + ay * ay + az * az)
        if mag < 0.001:
            return
        ax, ay, az = ax / mag, ay / mag, az / mag

        # Standard tilt angles from gravity vector
        roll  = math.atan2(ay, az)
        pitch = math.atan2(-ax, math.sqrt(ay * ay + az * az))

        # Three.js Euler XYZ:
        #   rx (around X) = forward/backward tilt  → pitch
        #   ry (around Y) = yaw offset             → pi/2 to face camera
        #   rz (around Z) = left/right tilt        → roll
        self._group.rotate(pitch, math.pi / 2, roll)
