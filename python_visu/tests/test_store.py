import pytest

from data.store import SensorStore


def _reading(val: float = 1.0, ts: int = 1_000_000) -> dict:
    return {
        "timestamp": ts,
        "accelerometer": {"x": val, "y": val, "z": val},
        "gyroscope":     {"x": val, "y": val, "z": val},
        "magnetometer":  {"x": val, "y": val, "z": val},
    }


class TestInitialState:
    def test_count_is_zero(self):
        assert SensorStore().count == 0

    def test_snapshot_is_none(self):
        assert SensorStore().snapshot() is None


class TestPush:
    def test_increments_count(self):
        store = SensorStore()
        store.push(_reading())
        assert store.count == 1

    def test_multiple_pushes(self):
        store = SensorStore()
        for _ in range(5):
            store.push(_reading())
        assert store.count == 5

    def test_snapshot_not_none_after_push(self):
        store = SensorStore()
        store.push(_reading())
        assert store.snapshot() is not None


class TestSnapshot:
    def test_latest_accelerometer_values(self):
        store = SensorStore()
        store.push(_reading(2.5))
        snap = store.snapshot()
        assert snap["latest"]["accelerometer"]["x"] == pytest.approx(2.5)
        assert snap["latest"]["accelerometer"]["y"] == pytest.approx(2.5)
        assert snap["latest"]["accelerometer"]["z"] == pytest.approx(2.5)

    def test_latest_gyroscope_values(self):
        store = SensorStore()
        store.push(_reading(0.75))
        snap = store.snapshot()
        assert snap["latest"]["gyroscope"]["z"] == pytest.approx(0.75)

    def test_latest_magnetometer_values(self):
        store = SensorStore()
        store.push(_reading(-1.5))
        snap = store.snapshot()
        assert snap["latest"]["magnetometer"]["y"] == pytest.approx(-1.5)

    def test_count_in_snapshot(self):
        store = SensorStore()
        store.push(_reading())
        store.push(_reading())
        assert store.snapshot()["count"] == 2

    def test_series_keys_present(self):
        store = SensorStore()
        store.push(_reading())
        snap = store.snapshot()
        assert set(snap["series"].keys()) == {"accel", "gyro", "mag"}

    def test_series_axis_keys_present(self):
        store = SensorStore()
        store.push(_reading())
        snap = store.snapshot()
        for grp in ("accel", "gyro", "mag"):
            assert set(snap["series"][grp].keys()) == {"x", "y", "z"}

    def test_ts_list_length_matches_pushes(self):
        store = SensorStore()
        for _ in range(3):
            store.push(_reading())
        snap = store.snapshot()
        assert len(snap["ts"]) == 3

    def test_snapshot_is_independent_copy(self):
        store = SensorStore()
        store.push(_reading(1.0))
        snap1 = store.snapshot()
        store.push(_reading(9.0))
        snap2 = store.snapshot()
        assert snap1["latest"]["accelerometer"]["x"] == pytest.approx(1.0)
        assert snap2["latest"]["accelerometer"]["x"] == pytest.approx(9.0)


class TestHistoryLimit:
    def test_series_capped_at_history(self):
        store = SensorStore(history=5)
        for i in range(10):
            store.push(_reading(float(i)))
        snap = store.snapshot()
        assert len(snap["series"]["accel"]["x"]) == 5

    def test_series_keeps_latest_values(self):
        store = SensorStore(history=3)
        for i in range(5):
            store.push(_reading(float(i)))
        snap = store.snapshot()
        assert list(snap["series"]["accel"]["x"]) == pytest.approx([2.0, 3.0, 4.0])

    def test_ts_capped_at_history(self):
        store = SensorStore(history=4)
        for i in range(7):
            store.push(_reading(ts=i * 1000))
        snap = store.snapshot()
        assert len(snap["ts"]) == 4
