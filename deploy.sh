#!/usr/bin/env bash

set -eo pipefail

APP_NAME="ubuntu-gateway"
CONTAINER_NAME="ubuntu-gateway"
LOCAL_REGISTRY_NAME="local-registry"
LOCAL_REGISTRY_IMAGE="registry:2"
REMOTE_APP_DIR_NAME="ubuntu-gateway"
IMAGE_NAME="localhost:5000/${APP_NAME}:latest"
HOST_PORT="6324"
CONTAINER_PORT="8080"
REDIS_PORT="6379"
if [ -n "${BASH_VERSION:-}" ]; then
  SCRIPT_SOURCE="${BASH_SOURCE[0]}"
else
  SCRIPT_SOURCE="$0"
fi

SCRIPT_DIR="$(cd "$(dirname "${SCRIPT_SOURCE}")" && pwd)"

log() {
  echo "[deploy] $1"
}

fail() {
  echo "[deploy] 错误: $1" >&2
  exit 1
}

require_env() {
  local name="$1"
  local value="${2:-}"
  if [[ -z "${value}" ]]; then
    fail "缺少环境变量 ${name}"
  fi
}

find_jar() {
  local jar
  jar="$(find "${SCRIPT_DIR}/target" -maxdepth 1 -type f -name '*.jar' ! -name '*original*.jar' | sort | tail -n 1)"
  if [[ -z "${jar}" ]]; then
    fail "没有找到可部署的 jar，请先在当前项目目录执行 Maven 打包，例如: mvn clean package -DskipTests"
  fi
  echo "${jar}"
}

require_env "REMOTE_USER" "${REMOTE_USER:-}"
require_env "REMOTE_HOST" "${REMOTE_HOST:-}"
require_env "REMOTE_PASSWORD" "${REMOTE_PASSWORD:-}"

JAR_PATH="$(find_jar)"
REMOTE_TARGET_DIR="/home/projects/${REMOTE_APP_DIR_NAME}"
REMOTE_JAR_PATH="${REMOTE_TARGET_DIR}/${APP_NAME}.jar"
REMOTE_DOCKERFILE_PATH="${REMOTE_TARGET_DIR}/Dockerfile"
REMOTE_TMP_DIR="/tmp/${REMOTE_APP_DIR_NAME}"
REMOTE_TMP_JAR_PATH="${REMOTE_TMP_DIR}/${APP_NAME}.jar"
REMOTE_TMP_DOCKERFILE_PATH="${REMOTE_TMP_DIR}/Dockerfile"
REMOTE_HOST_TARGET="${REMOTE_USER}@${REMOTE_HOST}"

log "开始部署 ${APP_NAME}"
log "脚本目录: ${SCRIPT_DIR}"
log "本地 jar 路径: ${JAR_PATH}"
log "远端目录: ${REMOTE_TARGET_DIR}"
log "远端临时目录: ${REMOTE_TMP_DIR}"
log "容器内 Redis 端口将指向宿主机 Redis: ${REDIS_PORT}"

if ! command -v ssh >/dev/null 2>&1; then
  fail "当前环境缺少 ssh 命令"
fi

if ! command -v scp >/dev/null 2>&1; then
  fail "当前环境缺少 scp 命令"
fi

if ! command -v sshpass >/dev/null 2>&1; then
  fail "当前环境缺少 sshpass，请先安装，例如: brew install hudochenkov/sshpass/sshpass"
fi

log "检查远端目录并创建部署目录"
sshpass -p "${REMOTE_PASSWORD}" \
  ssh -o StrictHostKeyChecking=no "${REMOTE_HOST_TARGET}" "mkdir -p '${REMOTE_TMP_DIR}'"

log "上传 Dockerfile 到远端临时目录"
sshpass -p "${REMOTE_PASSWORD}" \
  scp -o StrictHostKeyChecking=no "${SCRIPT_DIR}/Dockerfile" "${REMOTE_HOST_TARGET}:${REMOTE_TMP_DOCKERFILE_PATH}"

log "上传 jar 到远端临时目录并统一命名为 ${APP_NAME}.jar"
sshpass -p "${REMOTE_PASSWORD}" \
  scp -o StrictHostKeyChecking=no "${JAR_PATH}" "${REMOTE_HOST_TARGET}:${REMOTE_TMP_JAR_PATH}"

log "开始在远端构建和发布容器"
sshpass -p "${REMOTE_PASSWORD}" \
  ssh -o StrictHostKeyChecking=no "${REMOTE_HOST_TARGET}" \
  "REMOTE_PASSWORD='${REMOTE_PASSWORD}' REMOTE_TARGET_DIR='${REMOTE_TARGET_DIR}' REMOTE_JAR_PATH='${REMOTE_JAR_PATH}' REMOTE_DOCKERFILE_PATH='${REMOTE_DOCKERFILE_PATH}' REMOTE_TMP_DIR='${REMOTE_TMP_DIR}' REMOTE_TMP_JAR_PATH='${REMOTE_TMP_JAR_PATH}' REMOTE_TMP_DOCKERFILE_PATH='${REMOTE_TMP_DOCKERFILE_PATH}' IMAGE_NAME='${IMAGE_NAME}' CONTAINER_NAME='${CONTAINER_NAME}' LOCAL_REGISTRY_NAME='${LOCAL_REGISTRY_NAME}' LOCAL_REGISTRY_IMAGE='${LOCAL_REGISTRY_IMAGE}' HOST_PORT='${HOST_PORT}' CONTAINER_PORT='${CONTAINER_PORT}' APP_NAME='${APP_NAME}' REDIS_PORT='${REDIS_PORT}' bash -s" <<'REMOTE_SCRIPT'
set -euo pipefail

log() {
  echo "[remote-deploy] $1"
}

fail() {
  echo "[remote-deploy] 错误: $1" >&2
  exit 1
}

if ! command -v docker >/dev/null 2>&1; then
  fail "远端服务器未安装 Docker"
fi

sudo_exec() {
  printf '%s\n' "${REMOTE_PASSWORD}" | sudo -S "$@"
}

detect_host_gateway_ip() {
  local gateway_ip
  gateway_ip="$(sudo_exec docker network inspect bridge --format '{{range .IPAM.Config}}{{.Gateway}}{{end}}' 2>/dev/null || true)"
  if [[ -z "${gateway_ip}" ]]; then
    gateway_ip="$(ip route | awk '/default/ {print $3; exit}')"
  fi
  if [[ -z "${gateway_ip}" ]]; then
    fail "未能自动识别宿主机网关 IP，无法为容器配置 Redis 地址"
  fi
  echo "${gateway_ip}"
}

log "使用 sudo 创建正式部署目录"
sudo_exec mkdir -p "${REMOTE_TARGET_DIR}"

log "使用 sudo 移动 Dockerfile 到正式部署目录"
sudo_exec mv "${REMOTE_TMP_DOCKERFILE_PATH}" "${REMOTE_DOCKERFILE_PATH}"

log "使用 sudo 移动 jar 到正式部署目录"
sudo_exec mv "${REMOTE_TMP_JAR_PATH}" "${REMOTE_JAR_PATH}"

log "清理远端临时目录"
rm -rf "${REMOTE_TMP_DIR}" || true

REDIS_HOST_IP="$(detect_host_gateway_ip)"
log "检测到宿主机网关 IP: ${REDIS_HOST_IP}"

if sudo_exec docker ps --format '{{.Names}}' | grep -Fxq "${LOCAL_REGISTRY_NAME}"; then
  log "本地 Docker 仓库已运行: ${LOCAL_REGISTRY_NAME}"
elif sudo_exec docker ps -a --format '{{.Names}}' | grep -Fxq "${LOCAL_REGISTRY_NAME}"; then
  log "启动已有的本地 Docker 仓库容器: ${LOCAL_REGISTRY_NAME}"
  sudo_exec docker start "${LOCAL_REGISTRY_NAME}" >/dev/null
else
  log "创建并启动本地 Docker 仓库: ${LOCAL_REGISTRY_NAME}"
  sudo_exec docker run -d \
    -p 5000:5000 \
    --restart unless-stopped \
    --name "${LOCAL_REGISTRY_NAME}" \
    "${LOCAL_REGISTRY_IMAGE}" >/dev/null
fi

if sudo_exec docker ps --format '{{.Names}}' | grep -Fxq "${CONTAINER_NAME}"; then
  log "检测到运行中的旧容器，准备停止: ${CONTAINER_NAME}"
  sudo_exec docker stop "${CONTAINER_NAME}" >/dev/null
fi

if sudo_exec docker ps -a --format '{{.Names}}' | grep -Fxq "${CONTAINER_NAME}"; then
  log "删除旧容器: ${CONTAINER_NAME}"
  sudo_exec docker rm "${CONTAINER_NAME}" >/dev/null
fi

log "基于远端目录构建镜像: ${IMAGE_NAME}"
sudo_exec docker build \
  --build-arg JAR_FILE="${APP_NAME}.jar" \
  -t "${IMAGE_NAME}" \
  "${REMOTE_TARGET_DIR}"

log "启动新容器，端口映射 ${HOST_PORT}:${CONTAINER_PORT}"
sudo_exec docker run -d \
  --name "${CONTAINER_NAME}" \
  --restart unless-stopped \
  -p "${HOST_PORT}:${CONTAINER_PORT}" \
  -e "SPRING_REDIS_HOST=${REDIS_HOST_IP}" \
  -e "SPRING_REDIS_PORT=${REDIS_PORT}" \
  "${IMAGE_NAME}" >/dev/null

log "当前容器状态:"
sudo_exec docker ps --filter "name=${CONTAINER_NAME}" --format 'table {{.Names}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}'

log "最近 20 行容器日志:"
sudo_exec docker logs --tail 20 "${CONTAINER_NAME}" || true

log "部署完成"
REMOTE_SCRIPT

log "部署流程执行完毕"
