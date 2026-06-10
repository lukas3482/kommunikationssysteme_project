# Kommunikationssysteme Projekt — IMU Sensor Pipeline

IMU-Sensor-Daten vom Raspberry Pi live im Browser visualisieren.

```
Raspberry Pi  ──(MQTT)──►  Broker  ──(MQTT)──►  Python Dashboard
 (Java/Maven)                                     (NiceGUI)
```

| Komponente | Sprache | Zweck |
|---|---|---|
| `raspberry_sensor/` | Java 17 + Maven | Liest IMU-Daten via I2C (QMI8658 + AK09918) und publisht sie per MQTT |
| `python_visu/` | Python 3.11 | Live-Dashboard im Browser (NiceGUI + ECharts) |

---

## Voraussetzungen

| Tool | Version |
|---|---|
| JDK | 17 |
| Maven | 3.8+ |
| Python | 3.11+ |
| make | beliebig |

---

## Lokal bauen und testen

### Java bauen (Fat-JAR)

```bash
make build
```

Erzeugt `raspberry_sensor/target/raspberry_sensor-*-shaded.jar`.

### Java Unit-Tests ausführen

```bash
make test-java
```

### Java Statische Analyse (PMD)

```bash
make check-java
```

### Python-Abhängigkeiten installieren

```bash
make install-py
```

### Python Unit-Tests ausführen

```bash
make test-py
```

### Python Statische Analyse (ruff)

```bash
make check-py
```

### Alles auf einmal

```bash
make test    # alle Tests (Java + Python)
make check   # alle Analysen (Java + Python)
```

### Aufräumen

```bash
make clean
```

---

## Dashboard starten

```bash
cd python_visu
python main.py
```

Dann im Browser: [http://localhost:8080](http://localhost:8080)

Das Dashboard erwartet einen MQTT-Broker unter `localhost:1883`.  
Per Umgebungsvariable konfigurierbar:

```bash
MQTT_BROKER=192.168.178.150 MQTT_PORT=1883 python main.py
```

---

## Deployment auf den Raspberry Pi

```bash
PI_HOST=pi@192.168.178.42 make run
```

Das Makefile kopiert das Fat-JAR via `scp` auf den Pi und startet es per `ssh`.  
`PI_HOST` und `PI_DIR` können angepasst werden:

```bash
PI_HOST=pi@raspberrypi.local PI_DIR=/opt/sensor make run
```

Die Sensoradressen und den MQTT-Broker in
`raspberry_sensor/src/main/resources/config.properties` konfigurieren  
(oder eine externe `config.properties` neben der JAR ablegen — diese überschreibt
die gebündelte Konfiguration zur Laufzeit).

---

## Continuous Integration

GitHub Actions läuft bei jedem Push auf `main` und bei jedem Pull Request:

| Job | Schritte |
|---|---|
| **java** | `make build` → `make test-java` → `make check-java` |
| **python** | `make test-py` → `make check-py` |

Der `main`-Branch ist geschützt: direkte Pushes sind gesperrt, PRs können nur
gemergt werden, wenn beide CI-Jobs grün sind.
