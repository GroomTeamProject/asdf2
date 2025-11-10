
# Bastion Security Group
resource "aws_security_group" "bastion" {
  name        = "team02-bastion-sg"
  description = "Bastion host SSH access"
  vpc_id      = aws_vpc.team02_vpc.id

  # SSH 접근 허용
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = var.bastion_allowed_cidrs
    description = "SSH access to bastion"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "team02-bastion-sg"
  }
}

# RDS Security Group
resource "aws_security_group" "team02_rds_security_group" {
  name        = "team02-rds-security-group"
  description = "Security group for RDS MariaDB"
  vpc_id      = aws_vpc.team02_vpc.id

  # VPC 내부 서비스로부터의 접근 허용
  ingress {
    from_port = 3306
    to_port   = 3306
    protocol  = "tcp"
    security_groups = [
      aws_security_group.eks_node_group.id,
      aws_security_group.bastion.id
    ]
    description = "Allow from EKS nodes and bastion"
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
