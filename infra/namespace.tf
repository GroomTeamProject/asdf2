# 
# Route 53 Namespaces
# 

# Public Hosted Zone (외부 접근용)
data "aws_route53_zone" "team02_public" {
  count = var.public_domain_name != "" ? 1 : 0
  name  = var.public_domain_name
}

# ALB를 api 서브도메인으로 연결
# Kubernetes Service의 LoadBalancer 정보를 가져옴
data "kubernetes_service" "gateway" {
  metadata {
    name      = "gateway"
    namespace = "team02"
  }
}

# API Gateway 도메인 (외부 접근)
resource "aws_route53_record" "api" {
  count   = var.public_domain_name != "" ? 1 : 0
  zone_id = data.aws_route53_zone.team02_public[0].zone_id
  name    = "api.${var.public_domain_name}"
  type    = "CNAME"
  ttl     = 300
  records = [data.kubernetes_service.gateway.status[0].load_balancer[0].ingress[0].hostname]

  depends_on = [data.kubernetes_service.gateway, data.aws_route53_zone.team02_public]
}



# Route 53 Private Hosted Zone (내부 접근용)
resource "aws_route53_zone" "team02" {
  name = "team02.local"

  vpc {
    vpc_id = aws_vpc.team02_vpc.id
  }

  tags = {
    Name = "team02-dns-zone"
  }
}

# Kafka DNS 레코드
resource "aws_route53_record" "kafka" {
  zone_id = aws_route53_zone.team02.zone_id
  name    = "kafka.team02.local"
  type    = "A"
  ttl     = 300
  records = ["10.0.3.100"] # Kafka 고정 IP (Private Subnet A)
}

# RDS DNS 레코드
resource "aws_route53_record" "rds" {
  zone_id = aws_route53_zone.team02.zone_id
  name    = "rds.team02.local"
  type    = "CNAME"
  ttl     = 300
  records = [aws_db_instance.team02_mariadb.endpoint]
}
