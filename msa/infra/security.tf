resource "aws_security_group" "team02_security_group" {
  name        = "team02-security-group"
  description = "Allow HTTP"
  vpc_id      = aws_vpc.team02_vpc.id

  ingress {
    from_port   = 8085
    to_port     = 8085
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# RDS Security Group
resource "aws_security_group" "team02_rds_security_group" {
  name        = "team02-rds-security-group"
  description = "Security group for RDS MariaDB"
  vpc_id      = aws_vpc.team02_vpc.id

  # ECS 서비스들로부터의 접근 허용
  ingress {
    from_port = 3306
    to_port   = 3306
    protocol  = "tcp"
    security_groups = [
      aws_security_group.team02_security_group.id
    ]
    description = "Allow from ECS services"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "team02-rds-security-group"
  }
}
