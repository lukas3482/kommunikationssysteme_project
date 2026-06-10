JAVA_DIR := raspberry_sensor
PY_DIR   := python_visu
PI_HOST  ?= pi@raspberrypi.local
PI_DIR   ?= ~/sensor

.PHONY: all build test test-java test-py clean check check-java check-py install-py run

all: build

# ── Java / Maven ──────────────────────────────────────────────────────────────

build:
	cd $(JAVA_DIR) && mvn package -DskipTests -B -q

test-java:
	cd $(JAVA_DIR) && mvn test -B

check-java:
	cd $(JAVA_DIR) && mvn pmd:check -B

# ── Python ────────────────────────────────────────────────────────────────────

install-py:
	pip install -r $(PY_DIR)/requirements.txt \
	            -r $(PY_DIR)/requirements-dev.txt

test-py:
	cd $(PY_DIR) && python -m pytest tests/ -v

check-py:
	ruff check $(PY_DIR)/

# ── Kombiniert ────────────────────────────────────────────────────────────────

test: test-java test-py

check: check-java check-py

# ── Deployment auf den Pi (PI_HOST=user@host make run) ────────────────────────

run: build
	scp $(JAVA_DIR)/target/raspberry_sensor-*-shaded.jar \
	    $(PI_HOST):$(PI_DIR)/sensor.jar
	ssh $(PI_HOST) "java -jar $(PI_DIR)/sensor.jar"

# ── Aufräumen ─────────────────────────────────────────────────────────────────

clean:
	cd $(JAVA_DIR) && mvn clean -q
	find $(PY_DIR) -name "*.pyc" -delete
	find $(PY_DIR) -name "__pycache__" -type d -exec rm -rf {} + 2>/dev/null || true
	find $(PY_DIR) -name ".pytest_cache" -type d -exec rm -rf {} + 2>/dev/null || true
