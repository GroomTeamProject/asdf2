# ECR 리포지토리 생성
resource "aws_ecr_repository" "team02_repos" {
  for_each = toset([
    "gateway",
    "user-service",
    "owner-service",
    "order-service",
    "review-service",
    "notification-service",
    "delivery-service",
    "payment-service"
  ])

  name                 = "team02-${each.key}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = false
  }

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = {
    Name = "team02-${each.key}"
  }
}

# ECR 생명주기 정책 (오래된 이미지 자동 삭제)
resource "aws_ecr_lifecycle_policy" "team02_lifecycle" {
  for_each   = aws_ecr_repository.team02_repos
  repository = each.value.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep last 5 images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 5
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}

