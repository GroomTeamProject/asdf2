#!/usr/bin/env bash
set -e

# ==========================================
# Check environment variables
# ==========================================

if [ -z "$VITE_API_URL" ]; then
  echo "!!!env variable VITE_API_URL is not set"
  exit 1
fi

if [ -z "$AWS_S3_BUCKET_DEPLOY" ]; then
  echo "!!!env variable AWS_S3_BUCKET_DEPLOY is not set"
  exit 1
fi

if [ -z "$AWS_CLOUDFRONT_DISTRIBUTION_ID_DEPLOY" ]; then
  echo "!!!env variable AWS_CLOUDFRONT_DISTRIBUTION_ID_DEPLOY is not set"
  exit 1
fi

# ==========================================
# Build and deploy frontend
# ==========================================

npm run build

aws s3 sync dist/ s3://${AWS_S3_BUCKET_DEPLOY} --delete

aws s3 website s3://${AWS_S3_BUCKET_DEPLOY} --index-document index.html --error-document index.html

aws cloudfront create-invalidation --distribution-id ${AWS_CLOUDFRONT_DISTRIBUTION_ID_DEPLOY} --paths "/*"
