#!/usr/bin/env bash
set -euo pipefail

base_url="${BASE_URL:-http://localhost:8080}"
passed=0
total=0

expect_source() {
  local category="$1"
  local query="$2"
  local document_type="$3"
  local expected="$4"
  local response

  response=$(curl -fsS --get "${base_url}/api/admin/chunks" \
    --data-urlencode "q=${query}" \
    --data-urlencode "documentType=${document_type}")
  total=$((total + 1))
  if jq -e --arg source "${expected}" 'any(.source == $source)' <<<"${response}" >/dev/null; then
    passed=$((passed + 1))
    printf 'PASS  %-18s %s\n' "${category}" "${query}"
  else
    printf 'FAIL  %-18s %s\n' "${category}" "${query}"
  fi
}

expect_empty() {
  local category="$1"
  local query="$2"
  local document_type="$3"
  local response

  response=$(curl -fsS --get "${base_url}/api/admin/chunks" \
    --data-urlencode "q=${query}" \
    --data-urlencode "documentType=${document_type}")
  total=$((total + 1))
  if jq -e 'length == 0' <<<"${response}" >/dev/null; then
    passed=$((passed + 1))
    printf 'PASS  %-18s %s\n' "${category}" "${query}"
  else
    printf 'FAIL  %-18s %s\n' "${category}" "${query}"
  fi
}

expect_absent() {
  local category="$1"
  local query="$2"
  local document_type="$3"
  local forbidden_pattern="$4"
  local response

  response=$(curl -fsS --get "${base_url}/api/admin/chunks" \
    --data-urlencode "q=${query}" \
    --data-urlencode "documentType=${document_type}")
  total=$((total + 1))
  if jq -e --arg pattern "${forbidden_pattern}" 'all(.source | test($pattern) | not)' <<<"${response}" >/dev/null; then
    passed=$((passed + 1))
    printf 'PASS  %-18s %s\n' "${category}" "${query}"
  else
    printf 'FAIL  %-18s %s\n' "${category}" "${query}"
  fi
}

expect_source "Policy" "파손 팔레트는 어디에 보관해야 하나요?" "POLICY" "damaged-pallet-policy.md"
expect_source "Policy" "출고 보류 요청은 어떻게 처리하나요?" "POLICY" "outbound-hold-policy.md"
expect_empty "Unknown policy" "직원 휴가 규정 알려줘" "POLICY"
expect_source "Resolution case" "모서리가 깨지고 랩이 찢어진 팔레트 처리" "RESOLUTION_CASE" "SIM-A-001"
expect_source "Resolution case" "바코드 라벨이 물에 젖어 스캔되지 않는다" "RESOLUTION_CASE" "SIM-A-006"
expect_source "Resolution case" "시스템 위치와 실물 위치가 다르다" "RESOLUTION_CASE" "SIM-A-011"
expect_source "Resolution case" "냉동 팔레트 온도가 허용 범위를 초과했다" "RESOLUTION_CASE" "SIM-A-016"
expect_source "Resolution case" "출고 직전 수량이 맞지 않는다" "RESOLUTION_CASE" "SIM-A-021"
expect_absent "Reusable filter" "재사용 제외 검증 사례" "RESOLUTION_CASE" '^SIM-A-024$'
expect_absent "Authorization" "냉동기 정전으로 온도 상승" "RESOLUTION_CASE" '^SIM-B-'

printf '\nRetrieval Simulation Result\n'
printf '%s\n' '---------------------------'
printf 'Total  %d/%d\n' "${passed}" "${total}"

test "${passed}" -eq "${total}"
