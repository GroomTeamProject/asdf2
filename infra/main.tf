terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    tls = {
      source  = "hashicorp/tls"
      version = "~> 4.0"
    }
    http = {
      source  = "hashicorp/http"
      version = "~> 3.4"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.13"
    }
  }

  # Terraform State Bucket
  backend "s3" {
    bucket = "team02-terraform-state-bucket"
    key    = "team02/terraform.tfstate"
    region = "ap-northeast-2"
    
    # State Lock을 위한 DynamoDB
    dynamodb_table = "terraform-state-lock"
    encrypt        = true
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
