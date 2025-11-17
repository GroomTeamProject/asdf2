# ==========================================
# Route 53 Hosted Zones
# ==========================================

# Create Private Hosted Zone
resource "aws_route53_zone" "team02" {
  name = "team02.internal"

  vpc {
    vpc_id = aws_vpc.team02_vpc.id
  }

  tags = {
    Name = "team02-dns-zone"
  }
}

# Create Kafka DNS Record
resource "aws_route53_record" "kafka" {
  zone_id = aws_route53_zone.team02.zone_id
  name    = "kafka.team02.internal"
  type    = "A"
  ttl     = 300
  records = ["10.0.3.100"] # Kafka 고정 IP (Private Subnet A)
}

# Create RDS DNS Record
resource "aws_route53_record" "rds" {
  zone_id = aws_route53_zone.team02.zone_id
  name    = "rds.team02.internal"
  type    = "CNAME"
  ttl     = 300
  records = [aws_db_instance.team02_mariadb.address]
}

# ==========================================
# API Subdomain Record
# ==========================================

# Find existing Public Hosted Zone
data "aws_route53_zone" "public" {
  count = var.public_domain_name != "" ? 1 : 0
  name  = var.public_domain_name
}

# Get ALB DNS from Kubernetes Ingress
data "kubernetes_ingress_v1" "gateway" {
  metadata {
    name      = "gateway-ingress"
    namespace = "team02"
  }
}

# Create API Subdomain Record
resource "aws_route53_record" "api" {
  count   = var.public_domain_name != "" ? 1 : 0
  zone_id = data.aws_route53_zone.public[0].zone_id
  name    = "api.${var.public_domain_name}"
  type    = "A"

  alias {
    name                   = data.kubernetes_ingress_v1.gateway.status[0].load_balancer[0].ingress[0].hostname
    zone_id                = "ZWKZPGTI48KDX"
    evaluate_target_health = true
  }

  depends_on = [
    data.kubernetes_ingress_v1.gateway
  ]
}

# Get CloudFront Distribution
data "aws_cloudfront_distribution" "frontend" {
  id = "E1OTWVUFW20X6W" # CloudFront Distribution ID
}

# Create App Subdomain Record (CloudFront)
resource "aws_route53_record" "app" {
  count   = var.public_domain_name != "" ? 1 : 0
  zone_id = data.aws_route53_zone.public[0].zone_id
  name    = "app.${var.public_domain_name}"
  type    = "A"

  alias {
    name                   = data.aws_cloudfront_distribution.frontend.domain_name
    zone_id                = data.aws_cloudfront_distribution.frontend.hosted_zone_id
    evaluate_target_health = false
  }
}
