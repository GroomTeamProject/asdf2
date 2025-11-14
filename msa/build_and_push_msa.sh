#!/usr/bin/env bash
set -e


# ==========================================
# Check Parameters
# ==========================================

if [ -z "$1" ]; then
  echo "!!!parameter SERVICE_NAME is required"
  exit 1
fi

SERVICE_NAME="$1"

# ==========================================
# Check environment variables
# ==========================================

if [ -z "$DOCKERHUB_USERNAME" ]; then
  echo "!!!env variable DOCKERHUB_USERNAME is not set"
  exit 1
fi

if [ -z "$DOCKERHUB_PASSWORD" ]; then
  echo "!!!env variable DOCKERHUB_PASSWORD is not set"
  exit 1
fi

if [ -z "$CONTAINER_REGISTRY" ]; then
  echo "!!!env variable CONTAINER_REGISTRY is not set"
  exit 1
fi

# ==========================================
# Build & Test
# ==========================================

echo ">>> Building Gradle project: ${SERVICE_NAME}..."

./gradlew :${SERVICE_NAME}:build

echo ">>> Build completed"
echo ">>>Test report path: ${SERVICE_NAME}/build/reports/tests/test"

# ==========================================
# Docker Buildx Setup
# ==========================================

echo ">>> Setting up Docker Buildx..."

docker buildx create --use || docker buildx use default

# ==========================================
# Docker Hub Login
# ==========================================

echo ">>> Logging in to Docker Hub..."

echo "${DOCKERHUB_PASSWORD}" | docker login -u "${DOCKERHUB_USERNAME}" --password-stdin

# ==========================================
# Build & Push Docker Image
# ==========================================

SHA_TAG=$(git rev-parse HEAD)
IMAGE_LATEST="${CONTAINER_REGISTRY}/${DOCKERHUB_USERNAME}/team02-${SERVICE_NAME}:latest"
IMAGE_SHA="${CONTAINER_REGISTRY}/${DOCKERHUB_USERNAME}/team02-${SERVICE_NAME}:${SHA_TAG}"

echo ">>> Building & pushing Docker image.."

cd ${SERVICE_NAME}

docker buildx build \
  --platform linux/amd64 \
  --push \
  -t "${IMAGE_SHA}" \
  -t "${IMAGE_LATEST}" \
  .

echo ">>> Docker push completed"
echo "=========================================="
echo " Deployed:"
echo "   ${IMAGE_SHA}"
echo "   ${IMAGE_LATEST}"
echo "=========================================="
