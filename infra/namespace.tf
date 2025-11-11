# ==========================================
# Route 53 Hosted Zones
# ==========================================

# Create Private Hosted Zone
resource "aws_route53_zone" "team02" {
  name = "team02.local"

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
  name    = "kafka.team02.local"
  type    = "A"
  ttl     = 300
  records = ["10.0.3.100"] # Kafka 고정 IP (Private Subnet A)
}

# Create RDS DNS Record
resource "aws_route53_record" "rds" {
  zone_id = aws_route53_zone.team02.zone_id
  name    = "rds.team02.local"
  type    = "CNAME"
  ttl     = 300
  records = [aws_db_instance.team02_mariadb.endpoint]
}
