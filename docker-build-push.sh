#!/bin/bash
# Build and optionally push mqbridge image to Docker Hub.
#
# Usage:
#   ./docker-build-push.sh                    # build only (tag: mqbridge:latest)
#   ./docker-build-push.sh <dockerhub_user>   # build and push as <dockerhub_user>/mqbridge:latest
#
# For push: ensure you are logged in first:  docker login

set -e
IMAGE_NAME="mqbridge"
TAG="${TAG:-latest}"

if [ -n "$1" ]; then
  FULL_IMAGE="${1}/${IMAGE_NAME}:${TAG}"
  echo "Building ${FULL_IMAGE} ..."
  docker build -t "${FULL_IMAGE}" -t "${IMAGE_NAME}:${TAG}" .
  echo "Pushing ${FULL_IMAGE} ..."
  docker push "${FULL_IMAGE}"
  echo "Done. Image pushed to Docker Hub: ${FULL_IMAGE}"
else
  echo "Building ${IMAGE_NAME}:${TAG} (local only) ..."
  docker build -t "${IMAGE_NAME}:${TAG}" .
  echo "Done. Run with: docker run -p 8080:8080 ${IMAGE_NAME}:${TAG}"
  echo "To push to Docker Hub: ./docker-build-push.sh <your-dockerhub-username>"
fi
