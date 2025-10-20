terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "ap-northeast-2" # 서울 리전
}

data "aws_secretsmanager_secret" "team02_secret" {
  name = "team02-secret"
}

data "aws_secretsmanager_secret_version" "team02_secret" {
  secret_id = data.aws_secretsmanager_secret.team02_secret.id
}
