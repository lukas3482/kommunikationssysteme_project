import sys

import pytest


@pytest.fixture(autouse=True)
def _reload_config():
    sys.modules.pop("config", None)
    yield
    sys.modules.pop("config", None)


def test_default_broker(monkeypatch):
    monkeypatch.delenv("MQTT_BROKER", raising=False)
    import config
    assert config.BROKER == "localhost"


def test_custom_broker(monkeypatch):
    monkeypatch.setenv("MQTT_BROKER", "192.168.1.50")
    import config
    assert config.BROKER == "192.168.1.50"


def test_default_port(monkeypatch):
    monkeypatch.delenv("MQTT_PORT", raising=False)
    import config
    assert config.PORT == 1883


def test_custom_port(monkeypatch):
    monkeypatch.setenv("MQTT_PORT", "8883")
    import config
    assert config.PORT == 8883


def test_port_is_int(monkeypatch):
    monkeypatch.delenv("MQTT_PORT", raising=False)
    import config
    assert isinstance(config.PORT, int)


def test_topic_constant():
    import config
    assert config.TOPIC == "sensor/imu"


def test_history_is_positive():
    import config
    assert config.HISTORY > 0
