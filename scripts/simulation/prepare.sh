#!/usr/bin/env bash
set -euo pipefail

database_url="${SIMULATION_DB_URL:-postgresql://localhost/warehouse_helpdesk_simulation}"
psql_bin="${PSQL_BIN:-psql}"

"${psql_bin}" "${database_url}" -v ON_ERROR_STOP=1 \
  -f src/main/resources/schema.sql \
  -f scripts/simulation/seed.sql

echo "Simulation data prepared"
