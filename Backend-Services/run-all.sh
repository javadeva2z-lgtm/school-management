#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="${ROOT_DIR}/.run-logs"
EUREKA_URL="${EUREKA_URL:-http://localhost:8761}"

# Local development configuration. Existing environment values take precedence.
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"

export DEV_DB_URL="${DEV_DB_URL:-jdbc:mysql://localhost:3306/school_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}"
export DEV_DB_USERNAME="${DEV_DB_USERNAME:-root}"
export DEV_DB_PASSWORD="${DEV_DB_PASSWORD:-root}"
export DEV_EUREKA_URL="${DEV_EUREKA_URL:-${EUREKA_URL}/eureka/}"
export DEV_APP_BASE_URL="${DEV_APP_BASE_URL:-http://localhost:8000/ui}"
export DEV_JWT_SECRET_KEY="${DEV_JWT_SECRET_KEY:-dev-only-change-this-secret-with-32-chars}"
export DEV_CORS_ALLOWED_ORIGINS="${DEV_CORS_ALLOWED_ORIGINS:-http://localhost:4200,http://192.168.1.10:4200,http://localhost:8000}"
export DEV_JPA_DDL_AUTO="${DEV_JPA_DDL_AUTO:-validate}"
export DEV_JPA_SHOW_SQL="${DEV_JPA_SHOW_SQL:-false}"
export DEV_JWT_EXPIRATION_MS="${DEV_JWT_EXPIRATION_MS:-86400000}"

export DB_MAX_POOL_SIZE="${DB_MAX_POOL_SIZE:-20}"
export DB_MIN_IDLE="${DB_MIN_IDLE:-5}"
export DB_CONNECTION_TIMEOUT="${DB_CONNECTION_TIMEOUT:-20000}"
export JPA_FORMAT_SQL="${JPA_FORMAT_SQL:-true}"

export API_GATEWAY_PORT="${API_GATEWAY_PORT:-8000}"
export USER_SERVICE_PORT="${USER_SERVICE_PORT:-8001}"
export ACADEMIC_SERVICE_PORT="${ACADEMIC_SERVICE_PORT:-8002}"
export PAYMENT_SERVICE_PORT="${PAYMENT_SERVICE_PORT:-8003}"
export NOTIFICATION_SERVICE_PORT="${NOTIFICATION_SERVICE_PORT:-8004}"
export COMMUNICATION_SERVICE_PORT="${COMMUNICATION_SERVICE_PORT:-8005}"
export UTILITY_SERVICE_PORT="${UTILITY_SERVICE_PORT:-8006}"
export EUREKA_SERVER_PORT="${EUREKA_SERVER_PORT:-8761}"

export REDIS_HOST="${REDIS_HOST:-localhost}"
export REDIS_PORT="${REDIS_PORT:-6379}"
export GATEWAY_CORS_ALLOWED_ORIGINS="${GATEWAY_CORS_ALLOWED_ORIGINS:-http://localhost:3000}"
export GATEWAY_CORS_ALLOWED_METHODS="${GATEWAY_CORS_ALLOWED_METHODS:-GET,POST,PUT,PATCH,DELETE,OPTIONS}"
export GATEWAY_CORS_ALLOWED_HEADERS="${GATEWAY_CORS_ALLOWED_HEADERS:-*}"
export GATEWAY_CORS_ALLOW_CREDENTIALS="${GATEWAY_CORS_ALLOW_CREDENTIALS:-true}"
export FORWARD_HEADERS_STRATEGY="${FORWARD_HEADERS_STRATEGY:-framework}"

export PAYMENT_REMINDER_DAYS_BEFORE_DUE="${PAYMENT_REMINDER_DAYS_BEFORE_DUE:-3}"
export PAYMENT_REMINDER_DAYS_AFTER_DUE="${PAYMENT_REMINDER_DAYS_AFTER_DUE:-7}"
export PAYMENT_REMINDER_DAYS_AFTER_DUE_14="${PAYMENT_REMINDER_DAYS_AFTER_DUE_14:-14}"
export NOTIFICATION_EMAIL_FROM="${NOTIFICATION_EMAIL_FROM:-noreply@school-management.com}"
export NOTIFICATION_SMTP_SERVER="${NOTIFICATION_SMTP_SERVER:-smtp.gmail.com}"
export TWILIO_ACCOUNT_SID="${TWILIO_ACCOUNT_SID:-}"
export TWILIO_AUTH_TOKEN="${TWILIO_AUTH_TOKEN:-}"
export TWILIO_WHATSAPP_NUMBER="${TWILIO_WHATSAPP_NUMBER:-}"
export GCP_STORAGE_ENABLED="${GCP_STORAGE_ENABLED:-false}"
export GCS_BUCKET="${GCS_BUCKET:-school-files}"

export EUREKA_ENABLE_SELF_PRESERVATION="${EUREKA_ENABLE_SELF_PRESERVATION:-true}"
export EUREKA_EVICTION_INTERVAL_MS="${EUREKA_EVICTION_INTERVAL_MS:-5000}"

export CIRCUIT_BREAKER_SLIDING_WINDOW_SIZE="${CIRCUIT_BREAKER_SLIDING_WINDOW_SIZE:-10}"
export CIRCUIT_BREAKER_MINIMUM_NUMBER_OF_CALLS="${CIRCUIT_BREAKER_MINIMUM_NUMBER_OF_CALLS:-5}"
export CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD="${CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD:-50}"
export CIRCUIT_BREAKER_WAIT_DURATION="${CIRCUIT_BREAKER_WAIT_DURATION:-10s}"

SERVICE_MODULES=(
  "user-service"
  "academic-service"
  "payment-service"
  "notification-service"
  "communication-service"
  "utility-service"
  "api-gateway"
)

PIDS=()
SERVICE_NAMES=()

log() {
  printf '[run-all] %s\n' "$*"
}

fail() {
  printf '[run-all] ERROR: %s\n' "$*" >&2
  exit 1
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || fail "Required command not found: $1"
}

stop_services() {
  local exit_code=$?
  local pid
  trap - EXIT INT TERM
  for pid in "${PIDS[@]}"; do
    if kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null || true
    fi
  done
  wait 2>/dev/null || true

  if (( exit_code != 0 )); then
    printf '[run-all] Services stopped with exit code %d. Logs: %s\n' "$exit_code" "$LOG_DIR" >&2
    if [[ -e /dev/tty ]]; then
      read -r -p '[run-all] Press Enter to close this Git Bash window...' _ </dev/tty || true
    fi
  fi

  return "$exit_code"
}

wait_for_eureka() {
  local attempts=30
  while (( attempts > 0 )); do
    if curl --silent --fail "${EUREKA_URL}/eureka/apps" >/dev/null 2>&1; then
      log "Eureka Server is ready at ${EUREKA_URL}"
      return
    fi
    attempts=$((attempts - 1))
    sleep 2
  done
  fail "Eureka Server did not become ready"
}

start_jar() {
  local module="$1"
  local jar="${ROOT_DIR}/${module}/target/${module}-1.0.0.jar"
  local log_file="${LOG_DIR}/${module}.log"

  [[ -f "$jar" ]] || fail "Built JAR not found: ${jar}"
  log "Starting ${module}"
  java -jar "$jar" >"$log_file" 2>&1 &
  PIDS+=("$!")
  SERVICE_NAMES+=("$module")
}

require_command java
require_command mvn
require_command curl

cd "$ROOT_DIR"
mkdir -p "$LOG_DIR"
rm -f "${LOG_DIR}"/*.log
trap stop_services EXIT INT TERM

log "Cleaning and building all modules"
mvn clean package -DskipTests

start_jar "eureka-server"
wait_for_eureka

for module in "${SERVICE_MODULES[@]}"; do
  start_jar "$module"
  sleep 2
done

log "All services are running"
log "API Gateway: http://localhost:8000"
log "Eureka Dashboard: ${EUREKA_URL}"
log "Logs: ${LOG_DIR}"
log "Press Ctrl+C to stop all services"

while true; do
  for index in "${!PIDS[@]}"; do
    if ! kill -0 "${PIDS[$index]}" 2>/dev/null; then
      module="${SERVICE_NAMES[$index]}"
      log_file="${LOG_DIR}/${module}.log"
      printf '[run-all] ERROR: %s exited unexpectedly; recent log output:\n' "$module" >&2
      tail -n 20 "$log_file" >&2 || true
      fail "Service failure detected in ${module}; check ${log_file}"
    fi
  done
  sleep 5
done
