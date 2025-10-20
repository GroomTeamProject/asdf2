resource "aws_db_parameter_group" "team02_mariadb_params" {
  name   = "team02-mariadb-params"
  family = "mariadb11.4"

  parameter {
    name  = "collation_server"
    value = "utf8mb4_general_ci"
  }

  parameter {
    name  = "character_set_server"
    value = "utf8mb4"
  }

  parameter {
    name  = "character_set_client"
    value = "utf8mb4"
  }

  parameter {
    name  = "character_set_connection"
    value = "utf8mb4"
  }

  parameter {
    name  = "character_set_database"
    value = "utf8mb4"
  }

  parameter {
    name  = "character_set_results"
    value = "utf8mb4"
  }

  tags = {
    Name = "team02-mariadb-params"
  }
}

# RDS Subnet Group
resource "aws_db_subnet_group" "team02_db_subnet_group" {
  name = "team02-db-subnet-group"
  subnet_ids = [
    aws_subnet.team02_public_subnet_a.id,
    aws_subnet.team02_public_subnet_b.id
  ]

  tags = {
    Name = "team02-db-subnet-group"
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

# RDS Instance - MariaDB
resource "aws_db_instance" "team02_mariadb" {
  identifier     = "team02-mariadb"
  engine         = "mariadb"
  engine_version = "11.4"

  instance_class    = "db.t3.micro"
  allocated_storage = 20
  storage_type      = "gp3"

  username = var.db_username
  password = var.db_password

  # 인코딩 설정이 적용된 파라미터 그룹
  parameter_group_name = aws_db_parameter_group.team02_mariadb_params.name

  # 네트워크 설정
  db_subnet_group_name   = aws_db_subnet_group.team02_db_subnet_group.name
  vpc_security_group_ids = [aws_security_group.team02_rds_security_group.id]

  # 백업 설정
  backup_retention_period = 7
  backup_window           = "03:00-04:00"
  maintenance_window      = "mon:04:00-mon:05:00"

  # 삭제 보호 비활성화 (개발 환경)
  skip_final_snapshot = true
  deletion_protection = false

  tags = {
    Name        = "team02-mariadb"
    Environment = "dev"
  }
}

# outputs 
output "rds_endpoint" {
  description = "RDS instance endpoint"
  value       = aws_db_instance.team02_mariadb.endpoint
}

output "rds_connection_string" {
  description = "JDBC connection string for Spring Boot"
  value       = "jdbc:mariadb://${aws_db_instance.team02_mariadb.endpoint}/{DB_NAME}?characterEncoding=UTF-8&useUnicode=true&serverTimezone=Asia/Seoul"
  sensitive   = false
}
