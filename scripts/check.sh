#!/usr/bin/env bash
set -euo pipefail

base_url="${BASE_URL:-http://localhost:8080}"

curl -fsS "${base_url}/actuator/health" | jq -e '.status == "UP"' >/dev/null
curl -fsS --get "${base_url}/api/admin/chunks" \
  --data-urlencode 'q=파손 팔레트는 어디에 보관해야 하나요?' \
  | jq -e 'any(.source == "damaged-pallet-policy.md")' >/dev/null

echo "Smoke check passed"
