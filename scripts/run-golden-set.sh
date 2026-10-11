#!/usr/bin/env bash
set -euo pipefail

set -a
source .env
set +a

RUN_GOLDEN_SET=true \
WAREHOUSE_RAG_INGEST_ON_STARTUP=true \
./gradlew test --tests com.skala.warehouse_helpdesk.eval.GoldenSetEvaluationTests
